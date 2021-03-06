package rotmg.messaging.data;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import alde.flash.utils.IData;

public class ObjectData implements IData {

	public short objectType;
	public ObjectStatusData status;

	public ObjectData() {
		status = new ObjectStatusData();
	}

	@Override
	public void parseFromInput(DataInput in) throws IOException {
		objectType = in.readShort();
		status.parseFromInput(in);
	}

	@Override
	public void writeToOutput(DataOutput out) throws IOException {

		out.writeShort(objectType);
		status.writeToOutput(out);
	}

}
