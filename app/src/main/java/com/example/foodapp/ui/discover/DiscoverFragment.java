package com.example.foodapp.ui.discover;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.example.foodapp.FragManagerViewModel;
import com.example.foodapp.R;
import com.example.foodapp.utils.SampleDiscover;

import java.util.ArrayList;

public class DiscoverFragment extends Fragment {

    private static String TAG = "DiscoverFragment";
    private DiscoverViewModel discoverViewModel;

    private RecyclerView recyclerView;
    private RecyclerView.Adapter adapter;
    private FragManagerViewModel fragManagerViewModel;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        discoverViewModel =
                ViewModelProviders.of(this).get(DiscoverViewModel.class);
        View root = inflater.inflate(R.layout.fragment_discover, container, false);
        recyclerView = root.findViewById(R.id.discoverRecyvlerView);


        return root;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState){
        super.onActivityCreated(savedInstanceState);

        fragManagerViewModel =
                ViewModelProviders.of(getActivity()).get(FragManagerViewModel.class);

        fragManagerViewModel.setFragStatus(R.id.discoverFragment);

        StaggeredGridLayoutManager layoutManager = new StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.VERTICAL);
        SampleDiscover.SampleDiscoverData sampleData = new SampleDiscover.SampleDiscoverData(getActivity().getAssets());
        ArrayList<SampleDiscover> discoverList = sampleData.getDiscoverData();

        adapter = new DiscoverAdapter(discoverList);

        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);
    }
}