/*
 * Java Genetic Algorithm Library (@__identifier__@).
 * Copyright (c) @__year__@ Franz Wilhelmstötter
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Author:
 *    Franz Wilhelmstötter (franz.wilhelmstoetter@gmail.com)
 */
package io.jenetics.ext.moea;

import static io.jenetics.internal.math.base.clamp;

import java.util.Comparator;

import io.jenetics.ext.moea.Vecs.DoubleVec;
import io.jenetics.ext.moea.Vecs.IntVec;
import io.jenetics.ext.moea.Vecs.LongVec;
import io.jenetics.ext.moea.Vecs.ObjectVec;

/**
 * The {@code Vec} interface represents the fitness result of a multi-objective
 * fitness function. It also defines a set of static factory methods which
 * allows you to create {@code Vec} instance from a given {@code int[]},
 * {@code long[]} or {@code double[]} array.
 *
 * <pre>{@code
 * final Vec<double[]> point2D = Vec.of(0.1, 5.4);
 * final Vec<int[]> point3D = Vec.of(1, 2, 3);
 * }</pre>
 *
 * @implNote
 * Although the {@code Vec} interface extends the {@link Comparable} interface,
 * it violates its <em>general</em> contract. It <em>only</em>
 * implements the pareto <em>dominance</em> relation, which defines a partial
 * order. So, trying to sort a list of {@code Vec} objects, might lead
 * to an exception (thrown by the sorting method) at runtime.
 *
 * @param <T> the underlying data type, like {@code int[]} or {@code double[]}
 *
 * @see <a href="https://en.wikipedia.org/wiki/Pareto_efficiency">
 *     Pareto efficiency</a>
 *
 * @author <a href="mailto:franz.wilhelmstoetter@gmail.com">Franz Wilhelmstötter</a>
 * @version 4.1
 * @since 4.1
 */
public interface Vec<T> extends Comparable<Vec<T>> {

	/**
	 * Return the underlying data structure.
	 *
	 * @return the underlying data structure
	 */
	public T data();

	/**
	 * Return the number of vector elements.
	 *
	 * @return the number of vector elements
	 */
	public int length();

	/**
	 * Return the comparator for comparing the elements of this MO vector.
	 *
	 * @return the comparator for comparing the elements of this MO vector
	 */
	public ElementComparator<T> comparator();

	/**
	 * Return a function which calculates the element distance of a vector at a
	 * given element index.
	 *
	 * @return a function which calculates the element distance of a vector at a
	 *         given element index
	 */
	public ElementDistance<T> distance();

	/**
	 * Return the comparator which defines the (Pareto) dominance measure.
	 *
	 * @return the comparator which defines the (Pareto) dominance measure
	 */
	public Comparator<T> dominance();

	/* *************************************************************************
	 * Default methods derived from the methods above.
	 * ************************************************************************/

	/**
	 * Compares the {@code this} vector with the {@code other} at the given
	 * component {@code index}.
	 *
	 * @param other the other vector
	 * @param index the component index
	 * @return a negative integer, zero, or a positive integer as
	 *        {@code this[index]} is less than, equal to, or greater than
	 *        {@code other[index]}
	 * @throws NullPointerException if the {@code other} object is {@code null}
	 * @throws IllegalArgumentException if the {@code index} is out of the valid
	 *         range {@code [0, length())}
	 */
	public default int compare(final Vec<T> other, final int index) {
		return comparator().compare(data(), other.data(), index);
	}

	/**
	 * Calculates the distance between two vector elements at the given
	 * {@code index}.
	 *
	 * @param other the second vector
	 * @param index the vector element index
	 * @return the distance between two vector elements
	 * @throws NullPointerException if the {@code other} vector is {@code null}
	 * @throws IllegalArgumentException if the {@code index} is out of the valid
	 *         range {@code [0, length())}
	 */
	public default double distance(final Vec<T> other, final int index) {
		return distance().distance(data(), other.data(), index);
	}

	/**
	 * Calculates the <a href="https://en.wikipedia.org/wiki/Pareto_efficiency">
	 * <b>Pareto Dominance</b></a> of vector {@code value()} and {@code other}.
	 *
	 * @param other the other vector
	 * @return {@code 1} if <b>value()</b> ≻ <b>other</b>, {@code -1} if
	 *         <b>other</b> ≻ <b>value()</b> and {@code 0} otherwise
	 * @throws NullPointerException if the {@code other} vector is {@code null}
	 */
	public default int dominance(final Vec<T> other) {
		return dominance().compare(data(), other.data());
	}

	/**
	 * The default implementation uses the {@link #dominance(Vec)} function
	 * for defining a <b>partial</b> order of two vectors.
	 *
	 * @param other the other vector
	 * @return {@code 1} if <b>value()</b> ≻ <b>other</b>, {@code -1} if
	 *         <b>other</b> ≻ <b>value()</b> and {@code 0} otherwise
	 * @throws NullPointerException if the {@code other} vector is {@code null}
	 */
	@Override
	public default int compareTo(final Vec<T> other) {
		return dominance(other);
	}

	/* *************************************************************************
	 * Common 'dominance' methods.
	 * ************************************************************************/

	/**
	 * Calculates the <a href="https://en.wikipedia.org/wiki/Pareto_efficiency">
	 *     <b>Pareto Dominance</b></a> of the two vectors <b>u</b> and <b>v</b>.
	 *
	 * @see Pareto#dominance(Comparable[], Comparable[])
	 *
	 * @param u the first vector
	 * @param v the second vector
	 * @param <C> the element type of vector <b>u</b> and <b>v</b>
	 * @return {@code 1} if <b>u</b> ≻ <b>v</b>, {@code -1} if <b>v</b> ≻
	 *         <b>u</b> and {@code 0} otherwise
	 * @throws NullPointerException if one of the arguments is {@code null}
	 * @throws IllegalArgumentException if {@code u.length != v.length}
	 */
	public static <C extends Comparable<? super C>> int
	dominance(final C[] u, final C[] v) {
		return dominance(u, v, Comparator.naturalOrder());
	}

	/**
	 * Calculates the <a href="https://en.wikipedia.org/wiki/Pareto_efficiency">
	 *     <b>Pareto Dominance</b></a> of the two vectors <b>u</b> and <b>v</b>.
	 *
	 * @see Pareto#dominance(Object[], Object[], Comparator)
	 *
	 * @param u the first vector
	 * @param v the second vector
	 * @param comparator the element comparator which is used for calculating
	 *        the dominance
	 * @param <T> the element type of vector <b>u</b> and <b>v</b>
	 * @return {@code 1} if <b>u</b> ≻ <b>v</b>, {@code -1} if <b>v</b> ≻
	 *         <b>u</b> and {@code 0} otherwise
	 * @throws NullPointerException if one of the arguments is {@code null}
	 * @throws IllegalArgumentException if {@code u.length != v.length}
	 */
	public static <T> int
	dominance(final T[] u, final T[] v, final Comparator<? super T> comparator) {
		return Pareto.dominance(u, v, comparator);
	}

	/**
	 * Calculates the <a href="https://en.wikipedia.org/wiki/Pareto_efficiency">
	 *     <b>Pareto Dominance</b></a> of the two vectors <b>u</b> and <b>v</b>.
	 *
	 * @see Pareto#dominance(int[], int[])
	 *
	 * @param u the first vector
	 * @param v the second vector
	 * @return {@code 1} if <b>u</b> ≻ <b>v</b>, {@code -1} if <b>v</b> ≻
	 *         <b>u</b> and {@code 0} otherwise
	 * @throws NullPointerException if one of the arguments is {@code null}
	 * @throws IllegalArgumentException if {@code u.length != v.length}
	 */
	public static int dominance(final int[] u, final int[] v) {
		return Pareto.dominance(u, v);
	}

	/**
	 * Calculates the <a href="https://en.wikipedia.org/wiki/Pareto_efficiency">
	 *     <b>Pareto Dominance</b></a> of the two vectors <b>u</b> and <b>v</b>.
	 *
	 * @see Pareto#dominance(long[], long[])
	 *
	 * @param u the first vector
	 * @param v the second vector
	 * @return {@code 1} if <b>u</b> ≻ <b>v</b>, {@code -1} if <b>v</b> ≻
	 *         <b>u</b> and {@code 0} otherwise
	 * @throws NullPointerException if one of the arguments is {@code null}
	 * @throws IllegalArgumentException if {@code u.length != v.length}
	 */
	public static int dominance(final long[] u, final long[] v) {
		return Pareto.dominance(u, v);
	}

	/**
	 * Calculates the <a href="https://en.wikipedia.org/wiki/Pareto_efficiency">
	 *     <b>Pareto Dominance</b></a> of the two vectors <b>u</b> and <b>v</b>.
	 *
	 * @see Pareto#dominance(double[], double[])
	 *
	 * @param u the first vector
	 * @param v the second vector
	 * @return {@code 1} if <b>u</b> ≻ <b>v</b>, {@code -1} if <b>v</b> ≻
	 *         <b>u</b> and {@code 0} otherwise
	 * @throws NullPointerException if one of the arguments is {@code null}
	 * @throws IllegalArgumentException if {@code u.length != v.length}
	 */
	public static int dominance(final double[] u, final double[] v) {
		return Pareto.dominance(u, v);
	}

	/* *************************************************************************
	 * Static factory functions for wrapping ordinary arrays.
	 * ************************************************************************/

	/**
	 * Wraps the given array into a {@code Vec} object.
	 *
	 * @param array the wrapped array
	 * @param <C> the array element type
	 * @return the given array wrapped into a {@code Vec} object.
	 * @throws NullPointerException if one of the arguments is {@code null}
	 * @throws IllegalArgumentException if the {@code array} length is zero
	 */
	public static <C extends Comparable<? super C>>
	Vec<C[]> of(final C[] array) {
		return of(
			array,
			(u, v, i) -> clamp(u[i].compareTo(v[i]), -1, 1)
		);
	}

	/**
	 * Wraps the given array into a {@code Vec} object.
	 *
	 * @param array the wrapped array
	 * @param distance the array element distance measure
	 * @param <C> the array element type
	 * @return the given array wrapped into a {@code Vec} object.
	 * @throws NullPointerException if one of the arguments is {@code null}
	 * @throws IllegalArgumentException if the {@code array} length is zero
	 */
	public static <C extends Comparable<? super C>> Vec<C[]> of(
		final C[] array,
		final ElementDistance<C[]> distance
	) {
		return of(array, Comparator.naturalOrder(), distance);
	}

	/**
	 * Wraps the given array into a {@code Vec} object.
	 *
	 * @param array the wrapped array
	 * @param comparator the (natural order) comparator of the array elements
	 * @param distance the distance function between two vector elements
	 * @param <T> the array element type
	 * @return the given array wrapped into a {@code Vec} object.
	 * @throws NullPointerException if one of the arguments is {@code null}
	 * @throws IllegalArgumentException if the {@code array} length is zero
	 */
	public static <T> Vec<T[]> of(
		final T[] array,
		final Comparator<? super T> comparator,
		final ElementDistance<T[]> distance
	) {
		return new ObjectVec<>(array, comparator, distance);
	}

	/**
	 * Wraps the given array into a {@code Vec} object.
	 *
	 * @param array the wrapped array
	 * @return the given array wrapped into a {@code Vec} object.
	 * @throws NullPointerException if the given {@code array} is {@code null}
	 * @throws IllegalArgumentException if the {@code array} length is zero
	 */
	public static Vec<int[]> of(final int... array) {
		return new IntVec(array);
	}

	/**
	 * Wraps the given array into a {@code Vec} object.
	 *
	 * @param array the wrapped array
	 * @return the given array wrapped into a {@code Vec} object.
	 * @throws NullPointerException if the given {@code array} is {@code null}
	 * @throws IllegalArgumentException if the {@code array} length is zero
	 */
	public static Vec<long[]> of(final long... array) {
		return new LongVec(array);
	}

	/**
	 * Wraps the given array into a {@code Vec} object.
	 *
	 * @param array the wrapped array
	 * @return the given array wrapped into a {@code Vec} object.
	 * @throws NullPointerException if the given {@code array} is {@code null}
	 * @throws IllegalArgumentException if the {@code array} length is zero
	 */
	public static Vec<double[]> of(final double... array) {
		return new DoubleVec(array);
	}

}