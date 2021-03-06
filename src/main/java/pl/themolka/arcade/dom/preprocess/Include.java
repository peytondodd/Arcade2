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

import pl.themolka.arcade.dom.Document;
import pl.themolka.arcade.dom.Node;
import pl.themolka.arcade.dom.engine.EngineManager;

import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;

/**
 * Include documents from the global document repository.
 */
public class Include extends NodePreprocess
                     implements TreePreprocessHandler {
    private final Preprocess tree = new TreePreprocess(this);

    private final EngineManager engines;
    private final Preprocessor preprocessor;
    private final Path repository;

    public Include(EngineManager engines, Preprocessor preprocessor, Path repository) {
        this.engines = Objects.requireNonNull(engines, "engines cannot be null");
        this.preprocessor = Objects.requireNonNull(preprocessor, "preprocessor cannot be null");
        this.repository = Objects.requireNonNull(repository, "repository cannot be null");
    }

    @Override
    public void invoke(Node node) throws PreprocessException {
        this.tree.preprocess(node);
    }

    //
    // TreePreprocessHandler
    //

    @Override
    public List<Node> defineNode(Node parent) {
        return parent.children("include");
    }

    @Override
    public void invokeNode(Node node) throws PreprocessException {
        if (!node.hasParent()) {
            throw new PreprocessException(node, "Node must have its parent");
        }
        Node parent = node.getParent();

        String targetPath = node.getValue();
        if (targetPath == null) {
            throw new PreprocessException(node, "Target document path not specified");
        }

        Document target;
        try {
            target = ImportStage.readDocument(this.engines, node, this.repository.resolve(targetPath).toUri());
        } catch (InvalidPathException ex) {
            throw new PreprocessException(node, "Invalid document path: " + ex.getReason(), ex);
        }

        if (!target.hasPath()) {
            throw new PreprocessException(target, "Cannot resolve target document path");
        } else if (!target.hasRoot()) {
            throw new PreprocessException(target, "Target document is empty");
        }

        // Preprocess the target document.
        this.preprocessor.preprocess(target);

        Node.detach(node); // Remove the old <include> node.
        parent.add(target.getRoot().children()); // We don't support root node properties.
    }
}
