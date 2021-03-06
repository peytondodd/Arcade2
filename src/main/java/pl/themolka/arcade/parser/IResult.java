/*
 * Copyright 2018 Aleksander Jagiełło
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package pl.themolka.arcade.parser;

import pl.themolka.arcade.util.OptionalProvider;

import java.util.Optional;

/**
 * Result of a fine parsed, or failed T object.
 *
 * <table border="1">
 *   <caption>Summary of Result Methods</caption>
 *   <tr>
 *     <td></td>
 *     <td><em>{@link #get()}</em></td>
 *     <td><em>{@link #isPresent()}</em></td>
 *     <td><em>{@link #or(Object)}</em></td>
 *     <td><em>{@link #orNull()}</em></td>
 *     <td><em>{@link #orFail()}</em></td>
 *     <td><em>{@link #orDefault(Object)}</em></td>
 *     <td><em>{@link #orDefaultNull()}</em></td>
 *   </tr>
 *   <tr>
 *     <td><b>{@link EmptyResult}</b> <em>Empty input was provided</em></td>
 *     <td>{@link NullPointerException} thrown</td>
 *     <td>{@code false}</td>
 *     <td>given parameter</td>
 *     <td>{@code null}</td>
 *     <td>{@link ParserException} thrown</td>
 *     <td>given parameter</td>
 *     <td>{@code null}</td>
 *   </tr>
 *   <tr>
 *     <td><b>{@link FailResult}</b> <em>Object failed to parse</em></td>
 *     <td>{@link NullPointerException} thrown</td>
 *     <td>{@code false}</td>
 *     <td>given parameter</td>
 *     <td>{@code null}</td>
 *     <td>{@link ParserException} thrown</td>
 *     <td>{@link ParserException} thrown</td>
 *     <td>{@link ParserException} thrown</td>
 *   </tr>
 *   <tr>
 *     <td><b>{@link FineResult}</b> <em>Object parsed successfully</em></td>
 *     <td>parsed object</td>
 *     <td>{@code true}</td>
 *     <td>parsed object</td>
 *     <td>parsed object</td>
 *     <td>parsed object</td>
 *     <td>parsed object</td>
 *     <td>parsed object</td>
 *   </tr>
 * </table>
 */
public interface IResult<T> extends OptionalProvider<T> {
    /**
     * Get the result, never null.
     * @throws NullPointerException if there is no result.
     */
    T get();

    /**
     * Is the result present?
     */
    boolean isPresent();

    /**
     * Convert this result into an {@link Optional<T>}.
     */
    default Optional<T> optional() {
        return this.isPresent() ? Optional.of(this.get()) : Optional.empty();
    }

    /**
     * Return parsed result,
     * or return the given value, if the result is not provided.
     */
    default T or(T def) {
        return this.isPresent() ? this.get() : def;
    }

    /**
     * Return parsed result,
     * or return the given value, if the input was not provided.
     * @throws ParserException If input was provided but it failed to parse.
     */
    T orDefault(T def) throws ParserException;

    /**
     * Return parsed result,
     * or return null, if the input was not provided.
     * @throws ParserException If input was provided but it failed to parse.
     */
    default T orDefaultNull() throws ParserException {
        return this.orDefault(null);
    }

    /**
     * Return parsed result,
     * or throw {@link ParserException} if the result is not provided.
     * @throws ParserException If result is not provided.
     */
    default T orFail() throws ParserException {
        return this.orNull();
    }

    /**
     * Return parsed result,
     * or return null, if the result is not provided.
     */
    default T orNull() {
        return this.or(null);
    }
}
