package org.jetbrains.kotlinconf.data

import libs.*
import org.jetbrains.kotlinconf.util.*
import platform.Foundation.*
import platform.UIKit.*

fun UIImageView.loadUserIcon(url: String?) {
    val nsURL = url?.let { NSURL.URLWithString(it) }
    sd_setImageWithURL(nsURL, placeholderImage = PLACEHOLDER_IMAGE)
}

private val PLACEHOLDER_IMAGE = UIImage.imageNamed("user_default")?.circularImage()
