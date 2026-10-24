package org.telegram.tabgram;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;

import org.junit.Test;
import org.telegram.tgnet.TLRPC;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class StickerFiltersTest {

    @Test
    public void removeDocumentsStripsIdsFromPacksAndDropsEmptyPacks() {
        List<TLRPC.Document> documents = new ArrayList<>(Arrays.asList(document(1), document(2), document(3)));
        List<TLRPC.TL_stickerPack> packs = new ArrayList<>(Arrays.asList(pack(1L, 2L), pack(2L), pack(3L)));

        Set<Long> removed = StickerFilters.removeDocuments(documents, packs, d -> d.id == 2);

        assertEquals(new HashSet<>(Collections.singletonList(2L)), removed);
        assertEquals(Arrays.asList(1L, 3L), Arrays.asList(documents.get(0).id, documents.get(1).id));
        assertEquals(2, packs.size());
        assertEquals(Collections.singletonList(1L), packs.get(0).documents);
        assertEquals(Collections.singletonList(3L), packs.get(1).documents);
    }

    @Test
    public void filteredCopyLeavesInputUntouchedAndDropsNulls() {
        List<String> input = new ArrayList<>(Arrays.asList("a", "drop", "b"));

        List<String> result = StickerFilters.filteredCopy(input, s -> s.equals("drop") ? null : s.toUpperCase());

        assertEquals(Arrays.asList("a", "drop", "b"), input);
        assertEquals(Arrays.asList("A", "B"), result);
        assertNotSame(input, result);
    }

    @Test
    public void serverOrderAppendsHiddenIdsAfterVisibleOnes() {
        assertEquals(Arrays.asList(3L, 1L, 2L, 4L), StickerFilters.serverOrder(Arrays.asList(3L, 1L), Arrays.asList(1L, 2L, 3L, 4L)));
    }

    @Test
    public void setIdsKeepsListOrder() {
        TLRPC.TL_messages_stickerSet a = new TLRPC.TL_messages_stickerSet();
        a.set = new TLRPC.TL_stickerSet();
        a.set.id = 7;
        TLRPC.TL_messages_stickerSet b = new TLRPC.TL_messages_stickerSet();
        b.set = new TLRPC.TL_stickerSet();
        b.set.id = 5;
        assertEquals(Arrays.asList(7L, 5L), StickerFilters.setIds(Arrays.asList(a, b)));
    }

    @Test
    public void byVisibleOrderSortsUnknownIdsLastWithoutBreakingTimSort() {
        List<Long> visibleOrder = new ArrayList<>();
        for (long id = 30; id >= 1; id--) {
            visibleOrder.add(id);
        }
        List<Long> items = new ArrayList<>();
        for (long id = 1; id <= 40; id++) {
            items.add(id);
        }
        Collections.shuffle(items, new java.util.Random(42));

        Collections.sort(items, StickerFilters.byVisibleOrder(visibleOrder, Long::longValue));

        assertEquals(visibleOrder, items.subList(0, 30));
        assertEquals(new HashSet<>(Arrays.asList(31L, 32L, 33L, 34L, 35L, 36L, 37L, 38L, 39L, 40L)), new HashSet<>(items.subList(30, 40)));
    }

    private static TLRPC.Document document(long id) {
        TLRPC.Document document = new TLRPC.TL_document();
        document.id = id;
        return document;
    }

    private static TLRPC.TL_stickerPack pack(Long... ids) {
        TLRPC.TL_stickerPack pack = new TLRPC.TL_stickerPack();
        pack.documents.addAll(Arrays.asList(ids));
        return pack;
    }
}
