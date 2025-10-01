package com.mitchbarnett.wikibanktagintegration;

import okhttp3.OkHttpClient;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Field;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

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
        CountDownLatch latch = new CountDownLatch(8);
        AtomicBoolean oresResult = new AtomicBoolean(false);
        AtomicBoolean membersItemsResult = new AtomicBoolean(false);
        AtomicBoolean nonGEItemsResult = new AtomicBoolean(false);
        AtomicBoolean dt2Result1Result = new AtomicBoolean(false);
        AtomicBoolean dt2Result2Result = new AtomicBoolean(false);
        AtomicBoolean fakeCategoryResult = new AtomicBoolean(false);
        AtomicBoolean aggregateCategoryOrResult = new AtomicBoolean(false);
        AtomicBoolean aggregateCategoryAndResult = new AtomicBoolean(false);

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

        plugin.getCategoryIDs("Desert Treasure II - The Fallen Empire", ids -> {
            dt2Result1Result.set(ids.length > 0);
            latch.countDown();
        });

        plugin.getCategoryIDs("Desert_Treasure_II_-_The_Fallen_Empire", ids -> {
            dt2Result2Result.set(ids.length > 0);
            latch.countDown();
        });

        plugin.getCategoryIDs("Fake_category", ids -> {
            fakeCategoryResult.set(ids.length > 0);
            latch.countDown();
        });

        plugin.getCategoryIDs("Melee_weapons||Ranged_weapons||Magic_weapons", ids -> {
            aggregateCategoryOrResult.set(ids.length > 0);
            latch.countDown();
        });

        plugin.getCategoryIDs("Melee_armour&&Legs_slot_items", ids -> {
            aggregateCategoryAndResult.set(ids.length > 0);
            latch.countDown();
        });

        boolean completed = latch.await(20, TimeUnit.SECONDS);
        Assert.assertTrue("Timeout waiting for responses", completed);

        Assert.assertTrue("Failed to query by category 'ores'", oresResult.get());
        Assert.assertTrue("Failed to query by category 'Members' items'", membersItemsResult.get());
        Assert.assertTrue("Failed to query by category 'Non-GE items'", nonGEItemsResult.get());
        Assert.assertTrue("Failed to query by category 'Desert Treasure II - The Fallen Empire'", dt2Result1Result.get());
        Assert.assertTrue("Failed to query by category 'Desert_Treasure_II_-_The_Fallen_Empire'", dt2Result2Result.get());
        Assert.assertFalse("Queried a 'Fake_category' and got a result!", fakeCategoryResult.get());
        Assert.assertTrue("Failed to query by category 'Melee_weapons||Ranged_weapons||Magic_weapons'", aggregateCategoryOrResult.get());
        Assert.assertTrue("Failed to query by category 'Melee_armour&&Legs_slot_items'", aggregateCategoryAndResult.get());
    }

    @Test
    public void testQueryByMonster() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(7);
        AtomicBoolean impResult = new AtomicBoolean(false);
        AtomicBoolean dukeResult = new AtomicBoolean(false);
        AtomicBoolean runeDragonResult1 = new AtomicBoolean(false);
        AtomicBoolean runeDragonResult2 = new AtomicBoolean(false);
        AtomicBoolean fakeMonster = new AtomicBoolean(false);
        AtomicBoolean dagannothOrResult = new AtomicBoolean(false);
        AtomicBoolean dagannothAndResult = new AtomicBoolean(false);

        plugin.getDropIDs("imp", ids -> {
            impResult.set(ids.length > 0);
            latch.countDown();
        });

        plugin.getDropIDs("Duke Sucellus", ids -> {
            dukeResult.set(ids.length > 0);
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

        plugin.getDropIDs("Fake_monster", ids -> {
            fakeMonster.set(ids.length > 0);
            latch.countDown();
        });

        plugin.getDropIDs("Dagannoth Prime||Dagannoth Rex||Dagannoth Supreme", ids -> {
            dagannothOrResult.set(ids.length > 0);
            latch.countDown();
        });

        plugin.getDropIDs("Dagannoth Prime||Dagannoth Rex||Dagannoth Supreme", ids -> {
            dagannothAndResult.set(ids.length > 0);
            latch.countDown();
        });

        boolean completed = latch.await(20, TimeUnit.SECONDS);
        Assert.assertTrue("Timeout waiting for responses", completed);

        Assert.assertTrue("Failed to query by monster 'imp'", impResult.get());
        Assert.assertTrue("Failed to query by monster 'Duke Sucellus'", dukeResult.get());
        Assert.assertTrue("Failed to query by monster 'Rune dragon'", runeDragonResult1.get());
        Assert.assertTrue("Failed to query by monster 'Rune_dragon'", runeDragonResult2.get());
        Assert.assertFalse("Queried a 'Fake_monster' and got a result!", fakeMonster.get());
        Assert.assertTrue("Failed to query by monster 'Dagannoth Prime||Dagannoth Rex||Dagannoth Supreme'", dagannothOrResult.get());
        Assert.assertTrue("Failed to query by monster 'Dagannoth Prime&&Dagannoth Rex&&Dagannoth Supreme'", dagannothAndResult.get());
    }
}
