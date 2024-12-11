package socialnetwork.socialnetwork.domain;

import java.util.List;

public class Page<T>{
    private final List<T> items;
    private final int currentPage;
    private final int totalPages;
    private final int totalItems;

    public Page(List<T> items, int currentPage, int totalPages, int totalItems) {
        this.items = items;
        this.currentPage = currentPage;
        this.totalPages = totalPages;
        this.totalItems = totalItems;
    }

    public List<T> getItems() {
        return items;
    }

    public int getCurrentPage() {
        return currentPage;
    }

    public int getTotalPages() {
        return totalPages;
    }

    public int getTotalItems() {
        return totalItems;
    }
}
