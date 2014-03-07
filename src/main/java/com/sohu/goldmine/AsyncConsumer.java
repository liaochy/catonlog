package com.sohu.goldmine;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.concurrent.Executor;

import com.taobao.metamorphosis.Message;
import com.taobao.metamorphosis.client.MessageSessionFactory;
import com.taobao.metamorphosis.client.MetaMessageSessionFactory;
import com.taobao.metamorphosis.client.consumer.ConsumerConfig;
import com.taobao.metamorphosis.client.consumer.MessageConsumer;
import com.taobao.metamorphosis.client.consumer.MessageListener;

public class AsyncConsumer {

	public static void main(final String[] args) throws Exception {
		final MessageSessionFactory sessionFactory = new MetaMessageSessionFactory(
				Help.initMetaConfig());

		final String topic = JobConfig.getString("metaq.consumer.topic");
		final String group = JobConfig.getString("metaq.consumer.group");
		ConsumerConfig consumerConfig = new ConsumerConfig(group);
		consumerConfig.setMaxDelayFetchTimeInMills(100);
		consumerConfig.setConsumeFromMaxOffset();
		final MessageConsumer consumer = sessionFactory
				.createConsumer(consumerConfig);
		// subscribe topic
		final ElevatorSender sender = new ElevatorSender(
				JobConfig.getString("elevator.host"),
				JobConfig.getInt("elevator.port"),
				JobConfig.getString("elevator.topic"),
				JobConfig.getBoolean("elevator.json"),
				Charset.defaultCharset());
		sender.open();
		consumer.subscribe(topic, 1024 * 1024, new MessageListener() {

			@Override
			public void recieveMessages(final Message message) {
				try {
					sender.append(new String(message.getData()));
				} catch (IOException e) {
					e.printStackTrace();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}

			@Override
			public Executor getExecutor() {
				return null;
			}
		});
		// complete subscribe
		consumer.completeSubscribe();
	}

}
