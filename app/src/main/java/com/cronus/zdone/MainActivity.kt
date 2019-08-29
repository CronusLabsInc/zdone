package com.cronus.zdone

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import com.cronus.zdone.api.ApiTokenManager
import com.cronus.zdone.dagger.Injector
import com.cronus.zdone.dagger.ScreenInjector
import com.wealthfront.blend.Blend
import com.wealthfront.magellan.ActionBarConfig
import com.wealthfront.magellan.NavigationListener
import com.wealthfront.magellan.Navigator
import com.wealthfront.magellan.support.SingleActivity
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : SingleActivity(), NavigationListener {

    val apiTokenManager: ApiTokenManager

    init {
        apiTokenManager = Injector.get().apiTokenManager()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)
        if (!apiTokenManager.hasToken()) {
            return getNavigator().resetWithRoot(this, ScreenInjector.get().loginScreen())
        }
    }

    override fun createNavigator(): Navigator {
        return Navigator.withRoot(ScreenInjector.get().homeScreen()).loggingEnabled(true).build()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.settings -> getNavigator().goTo(ScreenInjector.get().settingsScreen())
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onNavigate(actionBarConfig: ActionBarConfig) {
        if (actionBarConfig.animated()) {
            val blend = Blend()
            blend {
                target(toolbar).animations {
                    if (actionBarConfig.visible()) {
                        expand()
                    } else {
                        collapse()
                    }
                }
            }.start()
        } else {
            toolbar.visibility = if (actionBarConfig.visible()) View.VISIBLE else View.GONE
        }
    }
}
