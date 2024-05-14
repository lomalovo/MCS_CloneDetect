package net.sourceforge.retriever.sample;

import java.io.IOException;
import java.util.List;
import net.sourceforge.retriever.collector.Collector;
import net.sourceforge.retriever.collector.CollectorEvent;
import net.sourceforge.retriever.collector.CollectorListener;
import net.sourceforge.retriever.collector.handler.Document;
import net.sourceforge.retriever.collector.resource.Resource;
import net.sourceforge.retriever.persistor.LuceneField;
import net.sourceforge.retriever.persistor.LucenePersistor;
import net.sourceforge.retriever.persistor.Persistent;

/**
 * This sample shows how to use Retriever's built-in mechanism for persistence with Lucene.
 * 
 * Retriever is told to connect in a local hard disk source, and to store the index in a folder
 * called 'index' whose path is relative to the path where the user executed the sample.
 */
public class PersistingCrawledDataWithLucene {

    public static void main(final String[] args) throws IOException {
        new PersistingCrawledDataWithLucene().persist();
    }

    private void persist() throws IOException {
        final LucenePersistor persistor = new LucenePersistor("index");
        final Collector collector = new Collector("file:/home/lucas/articles");
        collector.addResourceTypeToCollect("pdf");
        collector.addCollectorListener(new CollectorListener() {

            public void onStart() {
                System.out.println("Retriever started.");
                try {
                    persistor.open(true);
                } catch (final IOException e) {
                }
            }

            public void onCollect(final CollectorEvent event) {
                final List<Resource> collectedResources = event.getCollectedResources();
                for (Resource resource : collectedResources) {
                    try {
                        final Document document = resource.getData();
                        persistor.persist(new PersistentClass(document.getURL(), document.getContent()));
                    } catch (final Exception e) {
                    }
                }
            }

            public void onFinish() {
                try {
                    persistor.optimize();
                } catch (final IOException e) {
                }
                try {
                    persistor.close();
                } catch (final IOException e) {
                }
                System.out.println("Retriever done.");
            }

            public void onBrokenLink(final CollectorEvent collectorEvent) {
            }
        });
        collector.run();
    }

    @Persistent
    public class PersistentClass {

        private String key;

        private String value;

        public PersistentClass(final String key, final String value) {
            this.key = key;
            this.value = value;
        }

        @LuceneField(name = "url", indexed = true, tokenized = true, stored = true, compressed = false, key = true)
        public String getKey() {
            return this.key;
        }

        public void setKey(final String key) {
            this.key = key;
        }

        @LuceneField(name = "content", indexed = true, tokenized = true, stored = true, compressed = true, key = false)
        public String getValue() {
            return this.value;
        }

        public void setValue(final String value) {
            this.value = value;
        }
    }
}
