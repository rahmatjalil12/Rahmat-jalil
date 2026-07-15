package com.example.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.example.utils.HashUtils;

/**
 * Enterprise-grade SQLite Database Helper for the Hostel Management System.
 * Fully normalized tables and secure parameterized queries.
 */
public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "HostelManager.db";
    private static final int DATABASE_VERSION = 1;
    private static final String TAG = "DatabaseHelper";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.d(TAG, "Creating database tables...");

        // 1. Admins Table
        db.execSQL("CREATE TABLE admins (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "username TEXT UNIQUE NOT NULL," +
                "password TEXT NOT NULL," +
                "email TEXT," +
                "status TEXT DEFAULT 'ACTIVE'," +
                "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                "updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                ")");

        // 2. Students Table
        db.execSQL("CREATE TABLE students (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "photo_uri TEXT," +
                "full_name TEXT NOT NULL," +
                "father_name TEXT," +
                "mother_name TEXT," +
                "reg_number TEXT UNIQUE NOT NULL," +
                "roll_number TEXT," +
                "cnic TEXT," +
                "passport TEXT," +
                "email TEXT UNIQUE," +
                "phone TEXT," +
                "emergency_contact TEXT," +
                "address TEXT," +
                "city TEXT," +
                "department TEXT," +
                "program TEXT," +
                "semester TEXT," +
                "gender TEXT," +
                "blood_group TEXT," +
                "dob TEXT," +
                "guardian_name TEXT," +
                "guardian_contact TEXT," +
                "password TEXT NOT NULL," +
                "security_question TEXT," +
                "security_answer TEXT," +
                "registration_date TEXT," +
                "status TEXT DEFAULT 'ACTIVE'," +
                "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                "updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                ")");

        // 3. Blocks Table
        db.execSQL("CREATE TABLE blocks (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "name TEXT UNIQUE NOT NULL," +
                "type TEXT," + // Boys, Girls, Engineering, etc.
                "status TEXT DEFAULT 'ACTIVE'," +
                "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                "updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                ")");

        // 4. Floors Table
        db.execSQL("CREATE TABLE floors (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "block_id INTEGER," +
                "number INTEGER NOT NULL," +
                "status TEXT DEFAULT 'ACTIVE'," +
                "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                "updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                "FOREIGN KEY(block_id) REFERENCES blocks(id) ON DELETE CASCADE" +
                ")");

        // 5. Rooms Table
        db.execSQL("CREATE TABLE rooms (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "block_id INTEGER," +
                "floor_id INTEGER," +
                "room_number TEXT UNIQUE NOT NULL," +
                "capacity INTEGER NOT NULL," +
                "occupied_seats INTEGER DEFAULT 0," +
                "available_seats INTEGER," +
                "room_type TEXT," + // Single, Double, Triple, Four Sharing
                "status TEXT DEFAULT 'AVAILABLE'," + // AVAILABLE, OCCUPIED, MAINTENANCE, RESERVED
                "notes TEXT," +
                "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                "updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                "FOREIGN KEY(block_id) REFERENCES blocks(id) ON DELETE CASCADE," +
                "FOREIGN KEY(floor_id) REFERENCES floors(id) ON DELETE CASCADE" +
                ")");

        // 6. Room Allocation Table
        db.execSQL("CREATE TABLE allocations (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "room_id INTEGER," +
                "student_id INTEGER UNIQUE," +
                "allocation_date TEXT," +
                "status TEXT DEFAULT 'ACTIVE'," +
                "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                "updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                "FOREIGN KEY(room_id) REFERENCES rooms(id) ON DELETE CASCADE," +
                "FOREIGN KEY(student_id) REFERENCES students(id) ON DELETE CASCADE" +
                ")");

        // 7. Complaints Table
        db.execSQL("CREATE TABLE complaints (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "student_id INTEGER," +
                "category TEXT NOT NULL," +
                "title TEXT NOT NULL," +
                "description TEXT," +
                "status TEXT DEFAULT 'PENDING'," + // PENDING, PROCESSING, RESOLVED, REJECTED
                "remarks TEXT," +
                "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                "updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                "FOREIGN KEY(student_id) REFERENCES students(id) ON DELETE CASCADE" +
                ")");

        // 8. Leaves Table
        db.execSQL("CREATE TABLE leaves (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "student_id INTEGER," +
                "start_date TEXT NOT NULL," +
                "end_date TEXT NOT NULL," +
                "destination TEXT," +
                "reason TEXT," +
                "emergency_contact TEXT," +
                "status TEXT DEFAULT 'PENDING'," + // PENDING, APPROVED, REJECTED, CANCELLED
                "remarks TEXT," +
                "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                "updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                "FOREIGN KEY(student_id) REFERENCES students(id) ON DELETE CASCADE" +
                ")");

        // 9. Visitors Table
        db.execSQL("CREATE TABLE visitors (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "student_id INTEGER," +
                "name TEXT NOT NULL," +
                "relation TEXT," +
                "cnic TEXT," +
                "phone TEXT," +
                "visit_date TEXT," +
                "visit_time TEXT," +
                "purpose TEXT," +
                "status TEXT DEFAULT 'PENDING'," + // PENDING, APPROVED, REJECTED
                "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                "updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                "FOREIGN KEY(student_id) REFERENCES students(id) ON DELETE CASCADE" +
                ")");

        // 10. Fees Table
        db.execSQL("CREATE TABLE fees (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "student_id INTEGER," +
                "category TEXT NOT NULL," + // Hostel Fee, Mess Fee, Electricity, Security, Penalty
                "amount DOUBLE NOT NULL," +
                "status TEXT DEFAULT 'PENDING'," + // PAID, PENDING, PARTIALLY PAID
                "payment_date TEXT," +
                "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                "updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                "FOREIGN KEY(student_id) REFERENCES students(id) ON DELETE CASCADE" +
                ")");

        // 11. Notices Table
        db.execSQL("CREATE TABLE notices (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "title TEXT NOT NULL," +
                "description TEXT," +
                "priority TEXT," + // HIGH, MEDIUM, LOW
                "publish_date TEXT," +
                "expiry_date TEXT," +
                "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                "updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                ")");

        // 12. Staff Table
        db.execSQL("CREATE TABLE staff (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "name TEXT NOT NULL," +
                "role TEXT," + // Warden, Caretaker, Cleaner, Guard, Electrician
                "phone TEXT," +
                "email TEXT," +
                "shift TEXT," + // Morning, Evening, Night
                "status TEXT DEFAULT 'ACTIVE'," +
                "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                "updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                ")");

        // 13. Inventory Table
        db.execSQL("CREATE TABLE inventory (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "name TEXT NOT NULL," +
                "category TEXT," + // Furniture, Beds, Chairs, Table, Fan, Light, Mattress
                "total_stock INTEGER DEFAULT 0," +
                "available INTEGER DEFAULT 0," +
                "damaged INTEGER DEFAULT 0," +
                "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                "updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                ")");

        // Seed default Admin data
        seedInitialData(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS admins");
        db.execSQL("DROP TABLE IF EXISTS students");
        db.execSQL("DROP TABLE IF EXISTS blocks");
        db.execSQL("DROP TABLE IF EXISTS floors");
        db.execSQL("DROP TABLE IF EXISTS rooms");
        db.execSQL("DROP TABLE IF EXISTS allocations");
        db.execSQL("DROP TABLE IF EXISTS complaints");
        db.execSQL("DROP TABLE IF EXISTS leaves");
        db.execSQL("DROP TABLE IF EXISTS visitors");
        db.execSQL("DROP TABLE IF EXISTS fees");
        db.execSQL("DROP TABLE IF EXISTS notices");
        db.execSQL("DROP TABLE IF EXISTS staff");
        db.execSQL("DROP TABLE IF EXISTS inventory");
        onCreate(db);
    }

    private void seedInitialData(SQLiteDatabase db) {
        // Default admin credentials
        ContentValues adminValues = new ContentValues();
        adminValues.put("username", "admin");
        adminValues.put("password", HashUtils.hashSHA256("admin123"));
        adminValues.put("email", "admin@hostel.com");
        db.insert("admins", null, adminValues);

        // Prepopulate standard Blocks
        db.execSQL("INSERT INTO blocks (name, type, status) VALUES ('Block A (Boys)', 'Boys', 'ACTIVE')");
        db.execSQL("INSERT INTO blocks (name, type, status) VALUES ('Block B (Girls)', 'Girls', 'ACTIVE')");
        db.execSQL("INSERT INTO blocks (name, type, status) VALUES ('Engineering Hostel', 'Co-Ed', 'ACTIVE')");

        // Prepopulate Floors for Block A (Boys)
        db.execSQL("INSERT INTO floors (block_id, number, status) VALUES (1, 1, 'ACTIVE')");
        db.execSQL("INSERT INTO floors (block_id, number, status) VALUES (1, 2, 'ACTIVE')");

        // Prepopulate Floors for Block B (Girls)
        db.execSQL("INSERT INTO floors (block_id, number, status) VALUES (2, 1, 'ACTIVE')");

        // Prepopulate Rooms for Block 1 (A), Floor 1
        db.execSQL("INSERT INTO rooms (block_id, floor_id, room_number, capacity, occupied_seats, available_seats, room_type, status, notes) " +
                "VALUES (1, 1, 'A-101', 2, 0, 2, 'Double', 'AVAILABLE', 'Near the main gate')");
        db.execSQL("INSERT INTO rooms (block_id, floor_id, room_number, capacity, occupied_seats, available_seats, room_type, status, notes) " +
                "VALUES (1, 1, 'A-102', 3, 0, 3, 'Triple', 'AVAILABLE', 'Spacious windows')");
        db.execSQL("INSERT INTO rooms (block_id, floor_id, room_number, capacity, occupied_seats, available_seats, room_type, status, notes) " +
                "VALUES (1, 2, 'A-201', 1, 0, 1, 'Single', 'AVAILABLE', 'Premium Single room')");

        // Prepopulate Rooms for Block 2 (B), Floor 1
        db.execSQL("INSERT INTO rooms (block_id, floor_id, room_number, capacity, occupied_seats, available_seats, room_type, status, notes) " +
                "VALUES (2, 3, 'B-101', 4, 0, 4, 'Four Sharing', 'AVAILABLE', 'Quiet side block')");

        // Seed some initial staff
        db.execSQL("INSERT INTO staff (name, role, phone, email, shift, status) VALUES ('John Warden', 'Warden', '+1234567890', 'john@hostel.com', 'Morning', 'ACTIVE')");
        db.execSQL("INSERT INTO staff (name, role, phone, email, shift, status) VALUES ('Sara Caretaker', 'Caretaker', '+1987654321', 'sara@hostel.com', 'Evening', 'ACTIVE')");

        // Seed initial inventory
        db.execSQL("INSERT INTO inventory (name, category, total_stock, available, damaged) VALUES ('Wooden Bed Frame', 'Furniture', 100, 85, 2)");
        db.execSQL("INSERT INTO inventory (name, category, total_stock, available, damaged) VALUES ('Study Chair', 'Furniture', 150, 140, 5)");
        db.execSQL("INSERT INTO inventory (name, category, total_stock, available, damaged) VALUES ('Ceiling Fan', 'Fan', 80, 75, 3)");

        // Seed notice
        db.execSQL("INSERT INTO notices (title, description, priority, publish_date, expiry_date) " +
                "VALUES ('Welcome to New Academic Year', 'Hostel mess registrations are now open. Please clear your fees on time.', 'HIGH', '2026-07-15', '2026-08-15')");
    }
}
