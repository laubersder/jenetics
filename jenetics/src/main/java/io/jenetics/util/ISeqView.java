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
package io.jenetics.util;

import static java.util.Objects.requireNonNull;

import java.util.List;
import java.util.function.Function;

/**
 * ISeq view of an given list. The content is not copied on creation.
 *
 * @author <a href="mailto:franz.wilhelmstoetter@gmail.com">Franz Wilhelmstötter</a>
 * @version !__version__!
 * @since !__version__!
 */
final class ISeqView<T> implements ISeq<T> {

	private final List<? extends T> _list;

	ISeqView(final List<? extends T> list) {
		_list = requireNonNull(list);
	}

	@Override
	public T get(final int index) {
		return _list.get(index);
	}

	@Override
	public int length() {
		return _list.size();
	}

	@Override
	public ISeq<T> subSeq(final int start, final int end) {
		return new ISeqView<>(_list.subList(start, end));
	}

	@Override
	public ISeq<T> subSeq(final int start) {
		return new ISeqView<>(_list.subList(start, _list.size()));
	}

	@Override
	public <B> ISeq<B> map(final Function<? super T, ? extends B> mapper) {
		requireNonNull(mapper);

		final MSeq<B> result = MSeq.ofLength(length());
		for (int i = 0; i < length(); ++i) {
			result.set(i, mapper.apply(get(i)));
		}

		return result.toISeq();
	}

	@Override
	public ISeq<T> append(final Iterable<? extends T> values) {
		requireNonNull(values);
		return ISeq.<T>of(_list).append(values);
	}

	@Override
	public ISeq<T> prepend(final Iterable<? extends T> values) {
		requireNonNull(values);
		return ISeq.<T>of(_list).prepend(values);
	}

	@Override
	public MSeq<T> copy() {
		return MSeq.of(_list);
	}

}
