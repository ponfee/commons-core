package code.ponfee.commons.export;

import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicInteger;

import com.google.common.base.Preconditions;

import code.ponfee.commons.concurrent.MultithreadExecutor;
import code.ponfee.commons.util.Holder;

/**
 * Export multiple file
 *
 * @author fupf
 */
public abstract class AbstractSplitExporter extends AbstractExporter<Void> {

    private final int batchSize;
    private final String savingFilePathPrefix;
    private final String fileSuffix;
    private final ExecutorService executor;

    public AbstractSplitExporter(int batchSize, String savingFilePathPrefix, 
                                 String fileSuffix, ExecutorService executor) {
        Preconditions.checkArgument(batchSize > 0);
        this.batchSize = batchSize;
        this.savingFilePathPrefix = savingFilePathPrefix;
        this.fileSuffix = fileSuffix;
        this.executor = executor;
    }

    public @Override final void build(Table table) {
        CompletionService<Void> cs = new ExecutorCompletionService<>(executor);
        AtomicInteger count = new AtomicInteger(0);
        AtomicInteger split = new AtomicInteger(0);
        Holder<Table> subTable = Holder.of(table.copyOfWithoutTbody());
        rollingTbody(table, (data, i) -> {
            subTable.get().addRow(data);
            if (count.incrementAndGet() == batchSize) {
                super.nonEmpty();
                // sets a new table and return the last
                Table last = subTable.getAndSet(table.copyOfWithoutTbody());
                String path = buildFilePath(split.incrementAndGet());
                cs.submit(splitExporter(last, path), null);
                count.set(0); // reset count and sub table
            }
        });
        if (!subTable.get().isEmptyTbody()) {
            super.nonEmpty();
            String path = buildFilePath(split.incrementAndGet());
            cs.submit(splitExporter(subTable.get(), path), null);
        }

        MultithreadExecutor.joinDiscard(cs, split.get());
    }

    protected abstract AsnycSplitExporter splitExporter(
        Table subTable, String savingFilePath);

    public @Override final Void export() {
        throw new UnsupportedOperationException();
    }

    public @Override final void close() {}

    private String buildFilePath(int fileNo) {
        return savingFilePathPrefix + fileNo + fileSuffix;
    }

    public static abstract class AsnycSplitExporter implements Runnable {
        private final Table subTable;
        protected final String savingFilePath;

        public AsnycSplitExporter(Table subTable, String savingFilePath) {
            this.subTable = subTable;
            this.savingFilePath = savingFilePath;
        }

        @Override
        public final void run()  {
            subTable.end();
            try (AbstractExporter<?> exporter = createExporter()) {
                exporter.build(subTable);
                complete(exporter);
            }
        }

        protected abstract AbstractExporter<?> createExporter();

        protected void complete(AbstractExporter<?> exporter) {}
    }

}
