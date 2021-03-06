/*
 * Copyright 2011-2015 L2EMU UNIQUE
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.l2emuproject.proxy.network.game.server;

import static net.l2emuproject.proxy.network.game.client.L2GameClient.initializeCipher;

import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SocketChannel;
import java.util.Collections;
import java.util.Set;

import net.l2emuproject.network.protocol.IGameProtocolVersion;
import net.l2emuproject.network.protocol.ProtocolVersionManager;
import net.l2emuproject.network.security.EmptyCipher;
import net.l2emuproject.network.security.ICipher;
import net.l2emuproject.proxy.network.AbstractL2ServerProxy;
import net.l2emuproject.proxy.network.game.client.L2GameClient;

/**
 * Internally represents a game server connected to a L2 client.
 * 
 * @author savormix
 */
public final class L2GameServer extends AbstractL2ServerProxy
{
	private ICipher _cipher;
	private boolean _firstTime;
	
	private Set<String> _altModes;
	
	/**
	 * Creates an internal object representing a game server connection.
	 * 
	 * @param mmoController connection manager
	 * @param socketChannel connection
	 * @param target client that originally initiated this connection
	 * @throws ClosedChannelException if the given channel was closed during operations
	 */
	public L2GameServer(L2GameServerConnections mmoController, SocketChannel socketChannel, L2GameClient target) throws ClosedChannelException
	{
		super(mmoController, socketChannel, target);
		
		_cipher = EmptyCipher.getInstance();
		_firstTime = true;
		
		_altModes = Collections.emptySet();
	}
	
	@Override
	protected void onDisconnectionImpl()
	{
		// TODO Auto-generated method stub
		
	}
	
	@Override
	protected void onForcedDisconnection()
	{
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public boolean decipher(ByteBuffer buf)
	{
		if (isFirstTime())
		{
			setFirstTime(false);
			return true;
		}
		
		getCipher().decipher(buf);
		return true;
	}
	
	@Override
	public void encipher(ByteBuffer buf)
	{
		if (!isFirstTime())
		{
			getTargetClient().getDeobfuscator().encodeOpcodes(buf);
			getCipher().encipher(buf);
		}
	}
	
	@Override
	protected String getUID()
	{
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	protected boolean isAuthed()
	{
		return getCipher() != EmptyCipher.getInstance();
	}
	
	/**
	 * Initializes the cipher with a key part sent by the game server.
	 * 
	 * @param dynamicKeyPart key part
	 */
	public void initCipher(long dynamicKeyPart)
	{
		_cipher = initializeCipher(getProtocol(), dynamicKeyPart);
	}
	
	private ICipher getCipher()
	{
		return _cipher;
	}
	
	private boolean isFirstTime()
	{
		return _firstTime;
	}
	
	/**
	 * A method required for emulated (non-live) connections.
	 * 
	 * @param firstTime whether no packet has been deciphered yet
	 */
	public void setFirstTime(boolean firstTime)
	{
		_firstTime = firstTime;
	}
	
	/**
	 * Returns an internal object that represents a connected game client.
	 * 
	 * @return client object
	 */
	public L2GameClient getTargetClient()
	{
		return (L2GameClient)super.getTarget();
	}
	
	public Set<String> getAltModes()
	{
		return _altModes;
	}
	
	public void setAltModes(Set<String> altModes)
	{
		_altModes = altModes;
	}
	
	@Override
	public IGameProtocolVersion getProtocol()
	{
		final L2GameClient client = getTargetClient();
		if (client != null)
			return client.getProtocol();
		
		return ProtocolVersionManager.getInstance().getFallbackProtocolGame();
	}
	
	@Override
	public boolean ___supportsAheadOfTimeIntervention()
	{
		return false;
	}
}
