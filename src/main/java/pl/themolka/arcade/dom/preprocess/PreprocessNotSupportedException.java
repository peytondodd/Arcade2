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

package pl.themolka.arcade.dom.preprocess;

import pl.themolka.arcade.dom.Element;

public class PreprocessNotSupportedException extends PreprocessException {
    public PreprocessNotSupportedException(Element element) {
        super(element);
    }

    public PreprocessNotSupportedException(Element element, String message) {
        super(element, message);
    }

    public PreprocessNotSupportedException(Element element, String message, Throwable cause) {
        super(element, message, cause);
    }

    public PreprocessNotSupportedException(Element element, Throwable cause) {
        super(element, cause);
    }
}
