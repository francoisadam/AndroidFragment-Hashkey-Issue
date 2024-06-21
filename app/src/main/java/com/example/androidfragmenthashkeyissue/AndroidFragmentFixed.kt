package com.example.androidfragmenthashkeyissue

import android.os.Bundle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.Stable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.view.ViewCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentContainerView
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.commitNow

/*
This is a copy of the AndroidFragment composables to emphasis the issue and be able to compare it
with a quick fix
 */

@Composable
inline fun <reified T : Fragment> AndroidFragmentFixed(
    modifier: Modifier = Modifier,
    fragmentState: FragmentState = rememberFragmentState(),
    arguments: Bundle = Bundle.EMPTY,
    noinline onUpdate: (T) -> Unit = { }
) {
    AndroidFragmentFixed(
        clazz = T::class.java,
        modifier,
        fragmentState,
        arguments,
        onUpdate
    )
}

@Suppress("MissingJvmstatic")
@Composable
fun <T : Fragment> AndroidFragmentFixed(
    clazz: Class<T>,
    modifier: Modifier = Modifier,
    fragmentState: FragmentState = rememberFragmentState(),
    arguments: Bundle = Bundle.EMPTY,
    onUpdate: (T) -> Unit = { }
) {
    val updateCallback = rememberUpdatedState(onUpdate)

    /*
    This line is what I think is the reason of the demonstrated issue
    When using breakpoint inside the AndroidFragment implementation, we can see that this hashkey
    is the same for multiple fragments when using multiple AndroidFragment inside a Column.
    The consequence of that is these AndroidFragment will share the same container id, and thus
    override each other.
     */
    //val hashKey = currentCompositeKeyHash

    /*
     This is a quick fix but probably not a viable solution
     But it allows to compare the behavior with and without the fix for AndroidFragment
     */
    val hashKey = ViewCompat.generateViewId()

    val view = LocalView.current
    val fragmentManager = remember(view) {
        FragmentManager.findFragmentManager(view)
    }
    val context = LocalContext.current
    lateinit var container: FragmentContainerView
    AndroidView({
        container = FragmentContainerView(context)
        container.id = hashKey
        container
    }, modifier)

    DisposableEffect(fragmentManager, clazz, fragmentState) {
        val fragment = fragmentManager.findFragmentById(container.id)
            ?: fragmentManager.fragmentFactory.instantiate(
                context.classLoader, clazz.name
            ).apply {
                setInitialSavedState(fragmentState.state.value)
                setArguments(arguments)
                fragmentManager.beginTransaction()
                    .setReorderingAllowed(true)
                    .add(container, this, "$hashKey")
                    .commitNow()
            }
        fragmentManager.onContainerAvailable(container)
        @Suppress("UNCHECKED_CAST")
        updateCallback.value(fragment as T)
        onDispose {
            val state = fragmentManager.saveFragmentInstanceState(fragment)
            fragmentState.state.value = state
            if (!fragmentManager.isStateSaved) {
                // If the state isn't saved, that means that some state change
                // has removed this Composable from the hierarchy
                fragmentManager.commitNow {
                    remove(fragment)
                }
            }
        }
    }
}

@Composable
fun rememberFragmentState(): FragmentState {
    return rememberSaveable(saver = fragmentStateSaver()) {
        FragmentState()
    }
}

@Stable
class FragmentState(
    internal var state: MutableState<Fragment.SavedState?> = mutableStateOf(null)
)

private fun fragmentStateSaver(): Saver<FragmentState, *> = Saver(
    save = { it.state },
    restore = { FragmentState(it) }
)