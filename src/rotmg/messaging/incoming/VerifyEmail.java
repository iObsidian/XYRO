package rotmg.messaging.incoming;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import alde.flash.utils.consumer.MessageConsumer;

public class VerifyEmail extends IncomingMessage {

	public VerifyEmail(int id, MessageConsumer callback) {
		super(id, callback);
	}

	public void parseFromInput(DataInput in) throws IOException {
	}

	public void writeToOutput(DataOutput out) throws IOException {
	}
}