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

package pl.themolka.arcade.util.pagination;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DynamicPagination extends PrettyPagination<Paginationable> {
    @Override
    public String formatItem(int index, Paginationable item) {
        return this.getItem(index - 1).paginate(index);
    }

    public static class Builder implements org.apache.commons.lang3.builder.Builder<DynamicPagination> {
        private String description;
        private int extraItemsPerPage = EXTRA_ITEMS_PER_PAGE;
        private final List<Paginationable> items = new ArrayList<>();
        private int itemsPerPage = ITEMS_PER_PAGE;
        private String title;

        @Override
        public DynamicPagination build() {
            DynamicPagination pagination = new DynamicPagination();
            pagination.setDescription(this.description());
            pagination.setExtraItemsPerPage(this.extraItemsPerPage());
            pagination.setItems(this.items());
            pagination.setItemsPerPage(this.itemsPerPage());
            pagination.setTitle(this.title());
            return pagination;
        }

        public String description() {
            return this.description;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public int extraItemsPerPage() {
            return this.extraItemsPerPage;
        }

        public Builder extraItemsPerPage(int extraItemsPerPage) {
            this.extraItemsPerPage = extraItemsPerPage;
            return this;
        }

        public List<Paginationable> items() {
            return this.items;
        }

        public Builder items(List<Paginationable> items) {
            this.items.clear();
            this.items.addAll(items);

            Collections.sort(this.items);
            return this;
        }

        public int itemsPerPage() {
            return this.itemsPerPage;
        }

        public Builder itemsPerPage(int itemsPerPage) {
            this.itemsPerPage = itemsPerPage;
            return this;
        }

        public String title() {
            return this.title;
        }

        public Builder title(String title) {
            this.title = title;
            return this;
        }
    }
}
