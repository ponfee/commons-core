/* __________              _____                                                *\
** \______   \____   _____/ ____\____   ____    Copyright (c) 2017-2023 Ponfee  **
**  |     ___/  _ \ /    \   __\/ __ \_/ __ \   http://www.ponfee.cn            **
**  |    |  (  <_> )   |  \  | \  ___/\  ___/   Apache License Version 2.0      **
**  |____|   \____/|___|  /__|  \___  >\___  >  http://www.apache.org/licenses/ **
**                      \/          \/     \/                                   **
\*                                                                              */

package cn.ponfee.commons.export;

import cn.ponfee.commons.util.Holder;
import com.google.common.base.Preconditions;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

/**
 * Export multiple file
 *
 * @author Ponfee
 */
public abstract class AbstractSplitExporter extends AbstractDataExporter<Void> {

    private final int batchSize;
    private final String savingFilePathPrefix;
    private final String fileSuffix;
    private final Executor executor;

    public AbstractSplitExporter(int batchSize, String savingFilePathPrefix, 
                                 String fileSuffix, Executor executor) {
        Preconditions.checkArgument(batchSize > 0);
        this.batchSize = batchSize;
        this.savingFilePathPrefix = savingFilePathPrefix;
        this.fileSuffix = fileSuffix;
        this.executor = executor;
    }

    @Override
    public final <E> void build(Table<E> table) {
        List<CompletableFuture<Void>> futures = new LinkedList<>();
        AtomicInteger count = new AtomicInteger(0);
        AtomicInteger split = new AtomicInteger(0);
        Holder<Table<Object[]>> subTable = Holder.of(table.copyOfWithoutTbody(Function.identity()));
        rollingTbody(table, (data, i) -> {
            subTable.get().addRow(data);
            if (count.incrementAndGet() == batchSize) {
                // sets a new table and return the last
                Table<Object[]> last = subTable.set(table.copyOfWithoutTbody(Function.identity()));
                String path = buildFilePath(split.incrementAndGet());
                futures.add(CompletableFuture.runAsync(splitExporter(last, path), executor));
                count.set(0); // reset count and sub table
            }
        });
        if (!subTable.get().isEmptyTbody()) {
            String path = buildFilePath(split.incrementAndGet());
            futures.add(CompletableFuture.runAsync(splitExporter(subTable.get(), path), executor));
        }

        if (!futures.isEmpty()) {
            super.nonEmpty();
            futures.forEach(CompletableFuture::join);
        }
    }

    protected abstract AbstractAsyncSplitExporter splitExporter(Table<Object[]> subTable,
                                                                String savingFilePath);

    @Override
    public final Void export() {
        throw new UnsupportedOperationException();
    }

    private String buildFilePath(int fileNo) {
        return savingFilePathPrefix + String.format("%04d", fileNo) + fileSuffix;
    }

    public static abstract class AbstractAsyncSplitExporter implements Runnable {
        private final Table<Object[]> subTable;
        protected final String savingFilePath;

        public AbstractAsyncSplitExporter(Table<Object[]> subTable, String savingFilePath) {
            this.subTable = subTable;
            this.savingFilePath = savingFilePath;
        }

        @Override
        public final void run()  {
            subTable.toEnd();
            try (AbstractDataExporter<?> exporter = createExporter()) {
                exporter.build(subTable);
                complete(exporter);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        protected abstract AbstractDataExporter<?> createExporter() throws IOException;

        protected void complete(AbstractDataExporter<?> exporter) {}
    }

}
