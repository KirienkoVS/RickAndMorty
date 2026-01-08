package com.example.rickandmorty.ui.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.view.View
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.paging.LoadState
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.rickandmorty.R
import com.example.rickandmorty.ui.adapter.LoadStateAdapter
import kotlinx.coroutines.flow.collectLatest

internal fun isOnline(context: Context): Boolean {
    val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val network = connectivityManager.activeNetwork ?: return false
    val activeNetwork = connectivityManager.getNetworkCapabilities(network) ?: return false

    return when {
        activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
        activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
        else -> false
    }
}

internal fun restoreEditTextText(editTextList: List<EditText>, filterMap: Map<String, String>) {
    editTextList.forEach { editText ->
        filterMap.entries.forEach { filter ->
            if (editText.transitionName == filter.key) {
                editText.setText(filter.value)
            }
        }
    }
}

internal fun <T : Any> initLoadStateAdapter(
    isOnline: Boolean,
    textView: TextView,
    owner: LifecycleOwner,
    recyclerView: RecyclerView,
    activity: FragmentActivity?,
    swipeRefresh: SwipeRefreshLayout,
    pagingAdapter: PagingDataAdapter<T, RecyclerView.ViewHolder>,
) {
    val header = LoadStateAdapter { pagingAdapter.retry() }
    recyclerView.adapter = pagingAdapter.withLoadStateHeaderAndFooter(
        header = header,
        footer = LoadStateAdapter { pagingAdapter.retry() }
    )
    owner.lifecycleScope.launchWhenCreated {
        pagingAdapter.loadStateFlow.collectLatest { loadStates ->
            swipeRefresh.isRefreshing = loadStates.mediator?.refresh is LoadState.Loading
        }
    }
    pagingAdapter.addLoadStateListener { loadState ->
        val isListEmpty = loadState.refresh is LoadState.Error && pagingAdapter.itemCount == 0

        if (isListEmpty && isOnline) {
            textView.apply {
                visibility = View.VISIBLE
                text = resources.getString(R.string.no_match)
            }
        } else if (isListEmpty) {
            textView.isVisible = true
        } else textView.visibility = View.GONE

        header.loadState = loadState.mediator
            ?.refresh
            ?.takeIf { it is LoadState.Error && pagingAdapter.itemCount > 0 }
            ?: loadState.prepend

        val errorState = loadState.source.append as? LoadState.Error
            ?: loadState.source.prepend as? LoadState.Error
            ?: loadState.append as? LoadState.Error
            ?: loadState.prepend as? LoadState.Error
        errorState?.let {
            Toast.makeText(activity, activity?.resources?.getString(R.string.data_not_avaliable), Toast.LENGTH_LONG).show()
        }
    }
}
