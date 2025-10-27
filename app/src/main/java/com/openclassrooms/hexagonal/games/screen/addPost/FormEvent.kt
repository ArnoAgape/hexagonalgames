package com.openclassrooms.hexagonal.games.screen.addPost

import android.net.Uri

/**
 * A sealed class representing different events that can occur on a form.
 */
sealed class FormEvent {
  
  /**
   * Event triggered when the title of the form is changed.
   *
   * @property title The new title of the form.
   */
  data class TitleChanged(val title: String) : FormEvent()
  
  /**
   * Event triggered when the description of the form is changed.
   *
   * @property description The new description of the form.
   */
  data class DescriptionChanged(val description: String) : FormEvent()

  /**
   * Event triggered when the picture of the form is changed.
   *
   * @property photoUrl The url of the picture of the form.
   */
  data class PhotoChanged(val photoUrl: Uri?) : FormEvent()

  /**
   * Event triggered when the comment of the form is changed.
   *
   * @property comment The new comment of the form.
   */
  data class CommentChanged(val comment: String) : FormEvent()
  
}
