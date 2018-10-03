package rotmg.messaging.outgoing;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import alde.flash.utils.consumer.MessageConsumer;

public class SetCondition extends OutgoingMessage {

	public int conditionEffect;
	public double conditionDuration;

	public SetCondition(int id, MessageConsumer callback) {
		super(id, callback);
	}

	@Override
	public void parseFromInput(DataInput in) throws IOException {
		conditionEffect = in.readUnsignedByte();
		conditionDuration = in.readFloat();
	}

	@Override
	public void writeToOutput(DataOutput out) throws IOException {
		out.writeByte(conditionEffect);
		out.writeDouble(conditionDuration);
	}

}
