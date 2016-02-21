package com.quikr.platform.datastore.mysql.response;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class DBResponse implements Iterable<DBRow>{
	
	private List<DBRow> list;
	
	public DBResponse(){
		list = new ArrayList<DBRow>();
	}

	@Override
	public Iterator<DBRow> iterator() {
		return new DBResponseIterator();
	}
	
	public class DBResponseIterator implements Iterator<DBRow>{

		int index = 0;
		@Override
		public boolean hasNext() {
			return !(list.size() == index);
		}

		@Override
		public DBRow next() {
			//TODO: throw exception in case element does not exist;
			return list.get(index++);
		}

		@Override
		public void remove() {
			//Not Supported.
		}

	}
	
	public void add(DBRow row){
		list.add(row);
	}
	
	public int size(){
		return list.size();
	}

}
