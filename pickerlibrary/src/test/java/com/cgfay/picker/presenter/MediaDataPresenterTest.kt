package com.cgfay.picker.presenter

import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MediaDataPresenterTest {

    @Test
    fun add_and_remove_selected_media_updates_list_and_indices() {
        val presenter = MediaDataPresenter()
        val media1 = mockk<MediaData>(relaxed = true)
        val media2 = mockk<MediaData>(relaxed = true)

        // Initially list is empty
        assertTrue(presenter.getSelectedMediaDataList().isEmpty())

        // Add first media
        presenter.addSelectedMedia(media1)
        assertEquals(0, presenter.getSelectedIndex(media1))
        assertEquals(1, presenter.getSelectedMediaDataList().size)

        // Add second media
        presenter.addSelectedMedia(media2)
        assertEquals(1, presenter.getSelectedIndex(media2))
        assertEquals(2, presenter.getSelectedMediaDataList().size)

        // Remove first media
        presenter.removeSelectedMedia(media1)
        assertEquals(-1, presenter.getSelectedIndex(media1))
        assertEquals(1, presenter.getSelectedMediaDataList().size)
        assertEquals(0, presenter.getSelectedIndex(media2))

        // Clear remaining
        presenter.removeSelectedMedia(media2)
        assertTrue(presenter.getSelectedMediaDataList().isEmpty())
        assertEquals(-1, presenter.getSelectedIndex(media2))
    }
}
