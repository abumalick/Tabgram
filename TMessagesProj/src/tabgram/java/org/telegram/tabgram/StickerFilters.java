package org.telegram.tabgram;

import org.telegram.tgnet.TLRPC;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.ToLongFunction;

public final class StickerFilters {

    private StickerFilters() {}

    public static Set<Long> removeDocuments(List<TLRPC.Document> documents, List<TLRPC.TL_stickerPack> packs, Predicate<TLRPC.Document> remove) {
        Set<Long> removed = new HashSet<>();
        for (int i = documents.size() - 1; i >= 0; i--) {
            TLRPC.Document document = documents.get(i);
            if (remove.test(document)) {
                removed.add(document.id);
                documents.remove(i);
            }
        }
        if (removed.isEmpty()) {
            return removed;
        }
        for (int i = packs.size() - 1; i >= 0; i--) {
            TLRPC.TL_stickerPack pack = packs.get(i);
            pack.documents.removeAll(removed);
            if (pack.documents.isEmpty()) {
                packs.remove(i);
            }
        }
        return removed;
    }

    public static <T> ArrayList<T> filteredCopy(List<T> sets, Function<T, T> filter) {
        ArrayList<T> result = new ArrayList<>(sets.size());
        for (T set : sets) {
            T filtered = filter.apply(set);
            if (filtered != null) {
                result.add(filtered);
            }
        }
        return result;
    }

    public static List<Long> setIds(List<TLRPC.TL_messages_stickerSet> sets) {
        List<Long> ids = new ArrayList<>(sets.size());
        for (TLRPC.TL_messages_stickerSet set : sets) {
            ids.add(set.set.id);
        }
        return ids;
    }

    public static List<Long> serverOrder(List<Long> visibleOrder, List<Long> allIds) {
        LinkedHashSet<Long> order = new LinkedHashSet<>(visibleOrder);
        order.addAll(allIds);
        return new ArrayList<>(order);
    }

    public static <T> Comparator<T> byVisibleOrder(List<Long> visibleOrder, ToLongFunction<T> id) {
        Map<Long, Integer> positions = new HashMap<>();
        for (int i = 0; i < visibleOrder.size(); i++) {
            positions.put(visibleOrder.get(i), i);
        }
        return (a, b) -> {
            Integer pa = positions.get(id.applyAsLong(a));
            Integer pb = positions.get(id.applyAsLong(b));
            return Integer.compare(pa != null ? pa : Integer.MAX_VALUE, pb != null ? pb : Integer.MAX_VALUE);
        };
    }
}
