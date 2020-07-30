package com.example.fasterdelivery.Database;

import android.provider.BaseColumns;

public class ItemContract {
    private ItemContract() {}

    public static final class AddressEntry implements BaseColumns {
        public static final String TABLE_NAME = "AddressList";
        public static final String COLUMN_NAME = "name";
        public static final String COLUMN_NUMBER = "number";
        public static final String COLUMN_TIMESTAMP = "timestamp";
    }
}
