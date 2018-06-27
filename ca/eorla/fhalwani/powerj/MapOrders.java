package ca.eorla.fhalwani.powerj;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import javax.swing.JOptionPane;

class MapOrders {
	private short orderID = 0;
	private DataMasterOrder thisOrder = new DataMasterOrder();
	private DataOrderGroup thisGroup = new DataOrderGroup();
	private HashMap<Short, DataMasterOrder> orders = new HashMap<Short, DataMasterOrder>();
	private HashMap<Short, DataOrderGroup> groups = new HashMap<Short, DataOrderGroup>();
	
	MapOrders(PowerJ parent) {
		readDB(parent);
	}

	void close() {
		orders.clear();
		groups.clear();
	}
	
	short getCodeID(int id) {
		switch (id) {
		case 1:
			return thisGroup.code1;
		case 2:
			return thisGroup.code2;
		case 3:
			return thisGroup.code3;
		default:
			return thisGroup.code4;
		}
	}
	
	short getGroupID() {
		return thisOrder.groupID;
	}
	
	String getOrderName() {
		return thisGroup.name;
	}

	byte getOrderType() {
		return thisGroup.typeID;
	}

	/** Match a master-order from PowerPath to one from Derby */
	boolean matchOrder(short orderID) {
		if (thisOrder != null) {
			if (this.orderID == orderID) {
				return true;
			}
		}
		thisOrder = orders.get(orderID);
		if (thisOrder != null) {
			thisGroup = groups.get(thisOrder.groupID);
			if (thisGroup != null) {
				this.orderID = orderID;
				return true;
			}
		}
		return false;
	}
	
	private void readDB(PowerJ parent) {
		ResultSet rst = parent.dbPowerJ.getMasterOrders();
		try {
			while (rst.next()) {
				thisOrder = new DataMasterOrder();
				thisOrder.groupID = rst.getShort("GRPID");
				thisOrder.name = rst.getString("CODE");
				orders.put(rst.getShort("ID"), thisOrder);
			}
			rst.close();
			rst = parent.dbPowerJ.getGroups(0);
			while (rst.next()) {
				thisGroup = new DataOrderGroup();
				thisGroup.typeID = rst.getByte("GRP");
				thisGroup.code1 = rst.getShort("CODE1");
				thisGroup.code2 = rst.getShort("CODE2");
				thisGroup.code3 = rst.getShort("CODE3");
				thisGroup.code4 = rst.getShort("CODE4");
				thisGroup.name = rst.getString("NAME");
				groups.put(rst.getShort("ID"), thisGroup);
			}
		} catch (SQLException e) {
			parent.log(JOptionPane.ERROR_MESSAGE, "Orders Map", e);
		} finally {
			parent.dbPowerJ.closeRst(rst);
			parent.dbPowerJ.closeStm();
		}
	}
}
