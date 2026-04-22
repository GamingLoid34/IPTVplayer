package com.valladares.iptvplayer

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * Application entry point for Hilt's code generation and app-wide dependency container.
 *
 * This class is required so Hilt can create and manage the root component used by
 * activities, view models, and other Android classes that request dependency injection.
 */
@HiltAndroidApp
class IPTVApplication : Application()
