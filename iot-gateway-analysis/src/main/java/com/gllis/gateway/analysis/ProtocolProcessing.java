package com.gllis.gateway.analysis;

import com.gllis.gateway.server.domain.Packet;

public interface ProtocolProcessing {
	public void handler(Packet packet);
}
