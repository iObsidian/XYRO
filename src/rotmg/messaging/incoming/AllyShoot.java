package rotmg.messaging.incoming;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import alde.flash.utils.consumer.MessageConsumer;

public class AllyShoot extends IncomingMessage {

	public int bulletId;
	public int ownerId;
	public short containerType;
	public double angle;

	public AllyShoot(int id, MessageConsumer callback) {
		super(id, callback);
	}

	@Override
	public void parseFromInput(DataInput in) throws IOException {
		bulletId = in.readUnsignedByte();
		ownerId = in.readInt();
		containerType = in.readShort();
		angle = in.readFloat();
	}

	@Override
	public void writeToOutput(DataOutput out) throws IOException {
		out.writeByte(bulletId);
		out.writeInt(ownerId);
		out.writeShort(containerType);
		out.writeDouble(angle);
	}

}
