package com.sohu.goldmine;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import org.apache.avro.ipc.HttpTransceiver;
import org.apache.avro.ipc.Transceiver;
import org.apache.avro.ipc.specific.SpecificRequestor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sohu.goldmine.avro.AvroTopicEventServer;
import com.sohu.goldmine.avro.AvroTopicEventV2;

public class ElevatorSender {

	static final Logger LOG = LoggerFactory.getLogger(ElevatorSender.class);

	final public static String A_SERVERHOST = "serverHost";
	final public static String A_SERVERPORT = "serverPort";
	final public static String A_SENTBYTES = "sentBytes";

	protected AvroTopicEventServer avroClient;
	String host;
	int port;
	String topic;
	boolean isJson;
	Charset encode;
	AccountingTransceiver transport;

	public ElevatorSender(String host, int port, String topic, boolean isJson,
			Charset encode) {
		this.host = host;
		this.port = port;
		this.topic = topic;
		this.isJson = isJson;
		this.encode = encode;
	}

	public void append(String message) throws IOException, InterruptedException {
		List<AvroTopicEventV2> afe = toAvroListEventV2(message, topic, encode);
		// Make sure client side is initialized.
		this.ensureInitialized();
		try {
			avroClient.appendV2(afe, isJson);
		} catch (Exception e1) {
			throw new IOException("Append failed " + e1.getMessage(), e1);
		}
	}

	private static String SEPARATOR = "\\^_\\^!";

	public static List<AvroTopicEventV2> toAvroListEventV2(String message,
			String topic, Charset encode) throws IOException {
		String[] arr = message.split(SEPARATOR);
		List<AvroTopicEventV2> list = new ArrayList<AvroTopicEventV2>(
				arr.length);
		for (String one : arr) {
			list.add(toAvroEventV2(one, topic, encode));
		}
		return list;
	}

	public static AvroTopicEventV2 toAvroEventV2(String message, String topic,
			Charset encode) {
		AvroTopicEventV2 tempAvroEvt = new AvroTopicEventV2();
		tempAvroEvt.timestamp = System.currentTimeMillis();
		tempAvroEvt.body = message;
		tempAvroEvt.host = IpUtils.localhost();
		tempAvroEvt.topic = topic;
		tempAvroEvt.path = "";
		return tempAvroEvt;
	}

	private void ensureInitialized() throws IOException {
		if (this.avroClient == null || this.transport == null) {
			throw new IOException(
					"avroTopic called while not connected to server");
		}
	}

	public void open() throws IOException {

		URL url = new URL("http", host, port, "/");
		Transceiver http = new HttpTransceiver(url);
		transport = new AccountingTransceiver(http);
		try {
			this.avroClient = (AvroTopicEventServer) SpecificRequestor
					.getClient(AvroTopicEventServer.class, transport);
		} catch (Exception e) {
			throw new IOException("Failed to open Avro event sink at " + host
					+ ":" + port + " : " + e.getMessage());
		}
		LOG.info("AvroEventSink open on port  " + port);
	}

	public void close() throws IOException {
		if (transport != null) {
			transport.close();
			// we don't null out the transport, so getSentBytes can be called
			LOG.info("AvroEventSink on port " + port + " closed");
		} else {
			LOG.warn("Trying to close AvroEventSink, which was closed already");
		}
	}

	public long getSentBytes() {
		return transport.getSentBytes();
	}
}
