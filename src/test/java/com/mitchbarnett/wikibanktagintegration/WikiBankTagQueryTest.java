package com.mitchbarnett.wikibanktagintegration;

import com.mitchbarnett.wikibanktagintegration.WikiBankTagIntegrationPlugin;
import okhttp3.OkHttpClient;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Field;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

public class WikiBankTagQueryTest {

    private WikiBankTagIntegrationPlugin plugin;

    @Before
    public void initializeTestEnvironment() throws ReflectiveOperationException {
        this.plugin = new WikiBankTagIntegrationPlugin();

        Field httpClientField = WikiBankTagIntegrationPlugin.class.getDeclaredField("httpClient");
        httpClientField.setAccessible(true);
        httpClientField.set(this.plugin, new OkHttpClient());

        // Inject Gson instance
        Field gsonField = WikiBankTagIntegrationPlugin.class.getDeclaredField("gson");
        gsonField.setAccessible(true);
        gsonField.set(this.plugin, new com.google.gson.Gson());
    }

    @Test
    public void testQueryByCategory() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(3);
        AtomicBoolean oresResult = new AtomicBoolean(false);
        AtomicBoolean membersItemsResult = new AtomicBoolean(false);
        AtomicBoolean nonGEItemsResult = new AtomicBoolean(false);

        plugin.getCategoryIDs("ores", ids -> {
            oresResult.set(ids.length > 0);
            latch.countDown();
        });

        plugin.getCategoryIDs("Members' items", ids -> {
            membersItemsResult.set(ids.length > 0);
            latch.countDown();
        });

        plugin.getCategoryIDs("Non-GE items", ids -> {
            nonGEItemsResult.set(ids.length > 0);
            latch.countDown();
        });

        boolean completed = latch.await(10, TimeUnit.SECONDS);
        Assert.assertTrue("Timeout waiting for responses", completed);

        Assert.assertTrue("Failed to query by category 'ores'", oresResult.get());
        Assert.assertTrue("Failed to query by category 'Members' items'", membersItemsResult.get());
        Assert.assertTrue("Failed to query by category 'Non-GE items'", nonGEItemsResult.get());
    }

    @Test
    public void testQueryByMonster() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(3);
        AtomicBoolean impResult = new AtomicBoolean(false);
        AtomicBoolean runeDragonResult1 = new AtomicBoolean(false);
        AtomicBoolean runeDragonResult2 = new AtomicBoolean(false);

        plugin.getDropIDs("imp", ids -> {
            impResult.set(ids.length > 0);
            latch.countDown();
        });

        plugin.getDropIDs("Rune dragon", ids -> {
            runeDragonResult1.set(ids.length > 0);
            latch.countDown();
        });

        plugin.getDropIDs("Rune_dragon", ids -> {
            runeDragonResult2.set(ids.length > 0);
            latch.countDown();
        });

        boolean completed = latch.await(10, TimeUnit.SECONDS);
        Assert.assertTrue("Timeout waiting for responses", completed);

        Assert.assertTrue("Failed to query by monster 'imp'", impResult.get());
        Assert.assertTrue("Failed to query by monster 'Rune dragon'", runeDragonResult1.get());
        Assert.assertTrue("Failed to query by monster 'Rune_dragon'", runeDragonResult2.get());
    }
}
