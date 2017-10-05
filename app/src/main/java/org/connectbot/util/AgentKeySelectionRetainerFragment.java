package org.connectbot.util;

import org.connectbot.util.AgentKeySelection.AgentKeySelectionCallback;

import android.os.Bundle;
import android.support.v4.app.Fragment;

public class AgentKeySelectionRetainerFragment extends Fragment {
	public final static String TAG = "CB.AgentKeySelectionRetainerFragment";

	private AgentKeySelection mAgentKeySelection;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setRetainInstance(true);
	}

	public void setAgentKeySelection(AgentKeySelection agentKeySelection) {
		mAgentKeySelection = agentKeySelection;
	}

	public void setResultCallback(AgentKeySelectionCallback callback) {
		mAgentKeySelection.setResultCallback(callback);
	}
}
