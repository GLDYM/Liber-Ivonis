package com.liberivonis.client.screen;

import com.liberivonis.config.layout.HubLayout;
import com.liberivonis.config.layout.HubLayoutLoader;
import icyllis.modernui.fragment.Fragment;
import icyllis.modernui.view.LayoutInflater;
import icyllis.modernui.view.View;
import icyllis.modernui.view.ViewGroup;

/** The Hub is a ModernUI View tree described by config/liber_ivonis/hub.json. */
public final class IvonisHubFragment extends Fragment {
    private final boolean previewMode;

    public IvonisHubFragment() {
        this(false);
    }

    public IvonisHubFragment(boolean previewMode) {
        this.previewMode = previewMode;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            icyllis.modernui.util.DataSet savedInstanceState) {
        HubLayout layout = HubLayoutLoader.load();
        return new HubLayoutViewBuilder(requireContext(), layout, previewMode).build(layout);
    }
}
