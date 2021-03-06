package rotmg.messaging.outgoing;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import alde.flash.utils.consumer.MessageConsumer;


public class Buy extends OutgoingMessage {

	public int objectId;
	public int quantity;

	public Buy(int id, MessageConsumer callback) {
		super(id, callback);
	}

	@Override
	public void parseFromInput(DataInput in) throws IOException {
		objectId = in.readInt();
		quantity = in.readInt();
	}

	@Override
	public void writeToOutput(DataOutput out) throws IOException {
		out.writeInt(objectId);
		out.writeInt(quantity);
	}

}
