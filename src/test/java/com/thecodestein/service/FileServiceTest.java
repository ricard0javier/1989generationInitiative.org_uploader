package com.thecodestein.service;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.util.Set;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsCollectionContaining.hasItem;

public class FileServiceTest {

    public static final String ARTICLE_3_MD = "article_3.md";
    public static final String ARTICLE_2_MD = "article_2.md";
    public static final String ARTICLE_1_MD = "article_1.md";

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    private FileService target = new FileService();

    @Test
    public void testFindAll() throws Exception {

        folder.newFile(ARTICLE_1_MD);
        folder.newFile(ARTICLE_2_MD);
        folder.newFile(ARTICLE_3_MD);

        Set<String> files = target.getFiles(folder.getRoot());

        assertThat(files.size(), is(3));
        assertThat(files, hasItem(ARTICLE_1_MD));
        assertThat(files, hasItem(ARTICLE_2_MD));
        assertThat(files, hasItem(ARTICLE_3_MD));

    }
}

