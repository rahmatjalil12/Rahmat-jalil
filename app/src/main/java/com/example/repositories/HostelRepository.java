package com.example.repositories;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.database.DatabaseHelper;
import com.example.models.Block;
import com.example.models.Floor;
import com.example.models.Room;
import com.example.models.Student;
import com.example.models.Complaint;
import com.example.models.Leave;
import com.example.models.Notice;
import com.example.models.Visitor;
import com.example.models.Fee;
import com.example.models.Staff;
import com.example.models.Inventory;
import com.example.utils.HashUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Repository pattern implementation for managing all database transactions.
 * Uses robust cursor management to prevent memory leaks and parameterized queries.
 */
public class HostelRepository {

    private final DatabaseHelper dbHelper;

    public HostelRepository(Context context) {
        this.dbHelper = new DatabaseHelper(context);
    }

    // --- AUTHENTICATION ---

    public boolean checkAdminLogin(String username, String password) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String hashedPassword = HashUtils.hashSHA256(password);
        try (Cursor cursor = db.rawQuery("SELECT id FROM admins WHERE username = ? AND password = ? AND status = 'ACTIVE'",
                new String[]{username, hashedPassword})) {
            return cursor.moveToFirst();
        }
    }

    public boolean updateAdminPassword(String username, String newPassword) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("password", HashUtils.hashSHA256(newPassword));
        return db.update("admins", values, "username = ?", new String[]{username}) > 0;
    }

    public Student checkStudentLogin(String regNumber, String password) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String hashedPassword = HashUtils.hashSHA256(password);
        try (Cursor cursor = db.rawQuery("SELECT * FROM students WHERE reg_number = ? AND password = ? AND status = 'ACTIVE'",
                new String[]{regNumber, hashedPassword})) {
            if (cursor.moveToFirst()) {
                return cursorToStudent(cursor);
            }
        }
        return null;
    }

    public boolean resetStudentPassword(String regNumber, String securityQuestion, String securityAnswer, String newPassword) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        // verify answers
        try (Cursor cursor = db.rawQuery("SELECT id FROM students WHERE reg_number = ? AND security_question = ? AND security_answer = ?",
                new String[]{regNumber, securityQuestion, securityAnswer})) {
            if (cursor.moveToFirst()) {
                ContentValues values = new ContentValues();
                values.put("password", HashUtils.hashSHA256(newPassword));
                db.update("students", values, "reg_number = ?", new String[]{regNumber});
                return true;
            }
        }
        return false;
    }

    // --- BLOCK CRUD ---

    public boolean addBlock(Block block) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("name", block.getName());
        values.put("type", block.getType());
        values.put("status", block.getStatus());
        long result = db.insert("blocks", null, values);
        return result != -1;
    }

    public List<Block> getBlocks() {
        List<Block> list = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        try (Cursor cursor = db.rawQuery("SELECT * FROM blocks ORDER BY id DESC", null)) {
            while (cursor.moveToNext()) {
                list.add(new Block(
                        cursor.getInt(cursor.getColumnIndexOrThrow("id")),
                        cursor.getString(cursor.getColumnIndexOrThrow("name")),
                        cursor.getString(cursor.getColumnIndexOrThrow("type")),
                        cursor.getString(cursor.getColumnIndexOrThrow("status")),
                        cursor.getString(cursor.getColumnIndexOrThrow("created_at"))
                ));
            }
        }
        return list;
    }

    public boolean updateBlock(Block block) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("name", block.getName());
        values.put("type", block.getType());
        values.put("status", block.getStatus());
        return db.update("blocks", values, "id = ?", new String[]{String.valueOf(block.getId())}) > 0;
    }

    public boolean deleteBlock(int id) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        return db.delete("blocks", "id = ?", new String[]{String.valueOf(id)}) > 0;
    }

    // --- FLOOR CRUD ---

    public boolean addFloor(Floor floor) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("block_id", floor.getBlockId());
        values.put("number", floor.getNumber());
        values.put("status", floor.getStatus());
        return db.insert("floors", null, values) != -1;
    }

    public List<Floor> getFloorsByBlock(int blockId) {
        List<Floor> list = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        try (Cursor cursor = db.rawQuery("SELECT * FROM floors WHERE block_id = ? ORDER BY number ASC",
                new String[]{String.valueOf(blockId)})) {
            while (cursor.moveToNext()) {
                list.add(new Floor(
                        cursor.getInt(cursor.getColumnIndexOrThrow("id")),
                        cursor.getInt(cursor.getColumnIndexOrThrow("block_id")),
                        cursor.getInt(cursor.getColumnIndexOrThrow("number")),
                        cursor.getString(cursor.getColumnIndexOrThrow("status"))
                ));
            }
        }
        return list;
    }

    public boolean deleteFloor(int id) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        return db.delete("floors", "id = ?", new String[]{String.valueOf(id)}) > 0;
    }

    // --- ROOM CRUD ---

    public boolean addRoom(Room room) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("block_id", room.getBlockId());
        values.put("floor_id", room.getFloorId());
        values.put("room_number", room.getRoomNumber());
        values.put("capacity", room.getCapacity());
        values.put("occupied_seats", 0);
        values.put("available_seats", room.getCapacity());
        values.put("room_type", room.getRoomType());
        values.put("status", "AVAILABLE");
        values.put("notes", room.getNotes());
        return db.insert("rooms", null, values) != -1;
    }

    public List<Room> getRooms() {
        List<Room> list = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        try (Cursor cursor = db.rawQuery("SELECT * FROM rooms ORDER BY room_number ASC", null)) {
            while (cursor.moveToNext()) {
                list.add(cursorToRoom(cursor));
            }
        }
        return list;
    }

    public boolean updateRoom(Room room) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("room_number", room.getRoomNumber());
        values.put("capacity", room.getCapacity());
        values.put("room_type", room.getRoomType());
        values.put("status", room.getStatus());
        values.put("notes", room.getNotes());
        // update available seats too
        int avail = room.getCapacity() - room.getOccupiedSeats();
        values.put("available_seats", Math.max(0, avail));
        return db.update("rooms", values, "id = ?", new String[]{String.valueOf(room.getId())}) > 0;
    }

    public boolean deleteRoom(int id) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        return db.delete("rooms", "id = ?", new String[]{String.valueOf(id)}) > 0;
    }

    // --- STUDENT CRUD ---

    public boolean registerStudent(Student s) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues v = new ContentValues();
        v.put("photo_uri", s.getPhotoUri());
        v.put("full_name", s.getFullName());
        v.put("father_name", s.getFatherName());
        v.put("mother_name", s.getMotherName());
        v.put("reg_number", s.getRegNumber());
        v.put("roll_number", s.getRollNumber());
        v.put("cnic", s.getCnic());
        v.put("passport", s.getPassport());
        v.put("email", s.getEmail());
        v.put("phone", s.getPhone());
        v.put("emergency_contact", s.getEmergencyContact());
        v.put("address", s.getAddress());
        v.put("city", s.getCity());
        v.put("department", s.getDepartment());
        v.put("program", s.getProgram());
        v.put("semester", s.getSemester());
        v.put("gender", s.getGender());
        v.put("blood_group", s.getBloodGroup());
        v.put("dob", s.getDob());
        v.put("guardian_name", s.getGuardianName());
        v.put("guardian_contact", s.getGuardianContact());
        v.put("password", HashUtils.hashSHA256(s.getPassword()));
        v.put("security_question", s.getSecurityQuestion());
        v.put("security_answer", s.getSecurityAnswer());
        v.put("registration_date", s.getRegistrationDate());
        v.put("status", "ACTIVE");
        return db.insert("students", null, v) != -1;
    }

    public boolean updateStudent(Student s) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues v = new ContentValues();
        v.put("photo_uri", s.getPhotoUri());
        v.put("full_name", s.getFullName());
        v.put("father_name", s.getFatherName());
        v.put("mother_name", s.getMotherName());
        v.put("roll_number", s.getRollNumber());
        v.put("cnic", s.getCnic());
        v.put("passport", s.getPassport());
        v.put("email", s.getEmail());
        v.put("phone", s.getPhone());
        v.put("emergency_contact", s.getEmergencyContact());
        v.put("address", s.getAddress());
        v.put("city", s.getCity());
        v.put("department", s.getDepartment());
        v.put("program", s.getProgram());
        v.put("semester", s.getSemester());
        v.put("gender", s.getGender());
        v.put("blood_group", s.getBloodGroup());
        v.put("dob", s.getDob());
        v.put("guardian_name", s.getGuardianName());
        v.put("guardian_contact", s.getGuardianContact());
        v.put("status", s.getStatus());
        return db.update("students", v, "id = ?", new String[]{String.valueOf(s.getId())}) > 0;
    }

    public List<Student> getStudents() {
        List<Student> list = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        try (Cursor cursor = db.rawQuery("SELECT * FROM students ORDER BY full_name ASC", null)) {
            while (cursor.moveToNext()) {
                list.add(cursorToStudent(cursor));
            }
        }
        return list;
    }

    public Student getStudentById(int id) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        try (Cursor cursor = db.rawQuery("SELECT * FROM students WHERE id = ?", new String[]{String.valueOf(id)})) {
            if (cursor.moveToFirst()) {
                return cursorToStudent(cursor);
            }
        }
        return null;
    }

    public boolean deleteStudent(int id) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        // Deallocate first if any
        deallocateRoom(id);
        return db.delete("students", "id = ?", new String[]{String.valueOf(id)}) > 0;
    }

    // --- ROOM ALLOCATION ---

    public boolean allocateRoom(int roomId, int studentId, String date) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.beginTransaction();
        try {
            // Check room capacity
            int capacity = 0, occupied = 0;
            try (Cursor cursor = db.rawQuery("SELECT capacity, occupied_seats FROM rooms WHERE id = ?", new String[]{String.valueOf(roomId)})) {
                if (cursor.moveToFirst()) {
                    capacity = cursor.getInt(0);
                    occupied = cursor.getInt(1);
                }
            }

            if (occupied >= capacity) {
                return false; // Prevent over-allocation
            }

            // Remove previous allocation if any
            db.delete("allocations", "student_id = ?", new String[]{String.valueOf(studentId)});

            // Insert new allocation
            ContentValues val = new ContentValues();
            val.put("room_id", roomId);
            val.put("student_id", studentId);
            val.put("allocation_date", date);
            val.put("status", "ACTIVE");
            db.insert("allocations", null, val);

            // Update room occupied seats
            db.execSQL("UPDATE rooms SET occupied_seats = occupied_seats + 1, available_seats = capacity - (occupied_seats + 1) WHERE id = ?", new Object[]{roomId});

            db.setTransactionSuccessful();
            return true;
        } finally {
            db.endTransaction();
        }
    }

    public boolean deallocateRoom(int studentId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.beginTransaction();
        try {
            int roomId = -1;
            try (Cursor cursor = db.rawQuery("SELECT room_id FROM allocations WHERE student_id = ? AND status = 'ACTIVE'", new String[]{String.valueOf(studentId)})) {
                if (cursor.moveToFirst()) {
                    roomId = cursor.getInt(0);
                }
            }

            if (roomId == -1) return false;

            // Delete allocation
            db.delete("allocations", "student_id = ?", new String[]{String.valueOf(studentId)});

            // Decrement occupied seats
            db.execSQL("UPDATE rooms SET occupied_seats = MAX(0, occupied_seats - 1), available_seats = capacity - MAX(0, occupied_seats - 1) WHERE id = ?", new Object[]{roomId});

            db.setTransactionSuccessful();
            return true;
        } finally {
            db.endTransaction();
        }
    }

    public Room getRoomForStudent(int studentId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        try (Cursor cursor = db.rawQuery("SELECT r.* FROM rooms r INNER JOIN allocations a ON r.id = a.room_id WHERE a.student_id = ? AND a.status = 'ACTIVE'",
                new String[]{String.valueOf(studentId)})) {
            if (cursor.moveToFirst()) {
                return cursorToRoom(cursor);
            }
        }
        return null;
    }

    // --- COMPLAINTS ---

    public boolean addComplaint(Complaint c) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues val = new ContentValues();
        val.put("student_id", c.getStudentId());
        val.put("category", c.getCategory());
        val.put("title", c.getTitle());
        val.put("description", c.getDescription());
        val.put("status", "PENDING");
        return db.insert("complaints", null, val) != -1;
    }

    public List<Complaint> getComplaints() {
        List<Complaint> list = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        try (Cursor cursor = db.rawQuery("SELECT c.*, s.full_name as student_name FROM complaints c " +
                "INNER JOIN students s ON c.student_id = s.id ORDER BY c.id DESC", null)) {
            while (cursor.moveToNext()) {
                Complaint c = cursorToComplaint(cursor);
                c.setStudentName(cursor.getString(cursor.getColumnIndexOrThrow("student_name")));
                list.add(c);
            }
        }
        return list;
    }

    public List<Complaint> getComplaintsByStudent(int studentId) {
        List<Complaint> list = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        try (Cursor cursor = db.rawQuery("SELECT c.*, s.full_name as student_name FROM complaints c " +
                "INNER JOIN students s ON c.student_id = s.id WHERE c.student_id = ? ORDER BY c.id DESC", new String[]{String.valueOf(studentId)})) {
            while (cursor.moveToNext()) {
                Complaint c = cursorToComplaint(cursor);
                c.setStudentName(cursor.getString(cursor.getColumnIndexOrThrow("student_name")));
                list.add(c);
            }
        }
        return list;
    }

    public boolean updateComplaintStatus(int id, String status, String remarks) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues val = new ContentValues();
        val.put("status", status);
        val.put("remarks", remarks);
        return db.update("complaints", val, "id = ?", new String[]{String.valueOf(id)}) > 0;
    }

    // --- LEAVES ---

    public boolean addLeave(Leave l) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues val = new ContentValues();
        val.put("student_id", l.getStudentId());
        val.put("start_date", l.getStartDate());
        val.put("end_date", l.getEndDate());
        val.put("destination", l.getDestination());
        val.put("reason", l.getReason());
        val.put("emergency_contact", l.getEmergencyContact());
        val.put("status", "PENDING");
        return db.insert("leaves", null, val) != -1;
    }

    public List<Leave> getLeaves() {
        List<Leave> list = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        try (Cursor cursor = db.rawQuery("SELECT l.*, s.full_name as student_name FROM leaves l " +
                "INNER JOIN students s ON l.student_id = s.id ORDER BY l.id DESC", null)) {
            while (cursor.moveToNext()) {
                Leave l = cursorToLeave(cursor);
                l.setStudentName(cursor.getString(cursor.getColumnIndexOrThrow("student_name")));
                list.add(l);
            }
        }
        return list;
    }

    public List<Leave> getLeavesByStudent(int studentId) {
        List<Leave> list = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        try (Cursor cursor = db.rawQuery("SELECT l.*, s.full_name as student_name FROM leaves l " +
                "INNER JOIN students s ON l.student_id = s.id WHERE l.student_id = ? ORDER BY l.id DESC", new String[]{String.valueOf(studentId)})) {
            while (cursor.moveToNext()) {
                Leave l = cursorToLeave(cursor);
                l.setStudentName(cursor.getString(cursor.getColumnIndexOrThrow("student_name")));
                list.add(l);
            }
        }
        return list;
    }

    public boolean updateLeaveStatus(int id, String status, String remarks) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues val = new ContentValues();
        val.put("status", status);
        val.put("remarks", remarks);
        return db.update("leaves", val, "id = ?", new String[]{String.valueOf(id)}) > 0;
    }

    // --- VISITORS ---

    public boolean addVisitor(Visitor v) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues val = new ContentValues();
        val.put("student_id", v.getStudentId());
        val.put("name", v.getName());
        val.put("relation", v.getRelation());
        val.put("cnic", v.getCnic());
        val.put("phone", v.getPhone());
        val.put("visit_date", v.getVisitDate());
        val.put("visit_time", v.getVisitTime());
        val.put("purpose", v.getPurpose());
        val.put("status", "PENDING");
        return db.insert("visitors", null, val) != -1;
    }

    public List<Visitor> getVisitors() {
        List<Visitor> list = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        try (Cursor cursor = db.rawQuery("SELECT v.*, s.full_name as student_name FROM visitors v " +
                "INNER JOIN students s ON v.student_id = s.id ORDER BY v.id DESC", null)) {
            while (cursor.moveToNext()) {
                Visitor v = cursorToVisitor(cursor);
                v.setStudentName(cursor.getString(cursor.getColumnIndexOrThrow("student_name")));
                list.add(v);
            }
        }
        return list;
    }

    public List<Visitor> getVisitorsByStudent(int studentId) {
        List<Visitor> list = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        try (Cursor cursor = db.rawQuery("SELECT v.*, s.full_name as student_name FROM visitors v " +
                "INNER JOIN students s ON v.student_id = s.id WHERE v.student_id = ? ORDER BY v.id DESC", new String[]{String.valueOf(studentId)})) {
            while (cursor.moveToNext()) {
                Visitor v = cursorToVisitor(cursor);
                v.setStudentName(cursor.getString(cursor.getColumnIndexOrThrow("student_name")));
                list.add(v);
            }
        }
        return list;
    }

    public boolean updateVisitorStatus(int id, String status) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues val = new ContentValues();
        val.put("status", status);
        return db.update("visitors", val, "id = ?", new String[]{String.valueOf(id)}) > 0;
    }

    // --- FEES ---

    public boolean addFee(Fee f) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues val = new ContentValues();
        val.put("student_id", f.getStudentId());
        val.put("category", f.getCategory());
        val.put("amount", f.getAmount());
        val.put("status", f.getStatus());
        val.put("payment_date", f.getPaymentDate());
        return db.insert("fees", null, val) != -1;
    }

    public List<Fee> getFees() {
        List<Fee> list = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        try (Cursor cursor = db.rawQuery("SELECT f.*, s.full_name as student_name FROM fees f " +
                "INNER JOIN students s ON f.student_id = s.id ORDER BY f.id DESC", null)) {
            while (cursor.moveToNext()) {
                Fee f = cursorToFee(cursor);
                f.setStudentName(cursor.getString(cursor.getColumnIndexOrThrow("student_name")));
                list.add(f);
            }
        }
        return list;
    }

    public List<Fee> getFeesByStudent(int studentId) {
        List<Fee> list = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        try (Cursor cursor = db.rawQuery("SELECT f.*, s.full_name as student_name FROM fees f " +
                "INNER JOIN students s ON f.student_id = s.id WHERE f.student_id = ? ORDER BY f.id DESC", new String[]{String.valueOf(studentId)})) {
            while (cursor.moveToNext()) {
                Fee f = cursorToFee(cursor);
                f.setStudentName(cursor.getString(cursor.getColumnIndexOrThrow("student_name")));
                list.add(f);
            }
        }
        return list;
    }

    public boolean updateFeeStatus(int id, String status, String paymentDate) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues val = new ContentValues();
        val.put("status", status);
        val.put("payment_date", paymentDate);
        return db.update("fees", val, "id = ?", new String[]{String.valueOf(id)}) > 0;
    }

    // --- NOTICES ---

    public boolean addNotice(Notice n) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues val = new ContentValues();
        val.put("title", n.getTitle());
        val.put("description", n.getDescription());
        val.put("priority", n.getPriority());
        val.put("publish_date", n.getPublishDate());
        val.put("expiry_date", n.getExpiryDate());
        return db.insert("notices", null, val) != -1;
    }

    public List<Notice> getNotices() {
        List<Notice> list = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        try (Cursor cursor = db.rawQuery("SELECT * FROM notices ORDER BY id DESC", null)) {
            while (cursor.moveToNext()) {
                list.add(new Notice(
                        cursor.getInt(cursor.getColumnIndexOrThrow("id")),
                        cursor.getString(cursor.getColumnIndexOrThrow("title")),
                        cursor.getString(cursor.getColumnIndexOrThrow("description")),
                        cursor.getString(cursor.getColumnIndexOrThrow("priority")),
                        cursor.getString(cursor.getColumnIndexOrThrow("publish_date")),
                        cursor.getString(cursor.getColumnIndexOrThrow("expiry_date"))
                ));
            }
        }
        return list;
    }

    public boolean deleteNotice(int id) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        return db.delete("notices", "id = ?", new String[]{String.valueOf(id)}) > 0;
    }

    // --- STAFF ---

    public boolean addStaff(Staff s) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues val = new ContentValues();
        val.put("name", s.getName());
        val.put("role", s.getRole());
        val.put("phone", s.getPhone());
        val.put("email", s.getEmail());
        val.put("shift", s.getShift());
        val.put("status", "ACTIVE");
        return db.insert("staff", null, val) != -1;
    }

    public List<Staff> getStaffList() {
        List<Staff> list = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        try (Cursor cursor = db.rawQuery("SELECT * FROM staff ORDER BY name ASC", null)) {
            while (cursor.moveToNext()) {
                list.add(new Staff(
                        cursor.getInt(cursor.getColumnIndexOrThrow("id")),
                        cursor.getString(cursor.getColumnIndexOrThrow("name")),
                        cursor.getString(cursor.getColumnIndexOrThrow("role")),
                        cursor.getString(cursor.getColumnIndexOrThrow("phone")),
                        cursor.getString(cursor.getColumnIndexOrThrow("email")),
                        cursor.getString(cursor.getColumnIndexOrThrow("shift")),
                        cursor.getString(cursor.getColumnIndexOrThrow("status"))
                ));
            }
        }
        return list;
    }

    public boolean deleteStaff(int id) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        return db.delete("staff", "id = ?", new String[]{String.valueOf(id)}) > 0;
    }

    // --- INVENTORY ---

    public boolean addInventory(Inventory i) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues val = new ContentValues();
        val.put("name", i.getName());
        val.put("category", i.getCategory());
        val.put("total_stock", i.getTotalStock());
        val.put("available", i.getAvailable());
        val.put("damaged", i.getDamaged());
        return db.insert("inventory", null, val) != -1;
    }

    public boolean updateInventory(Inventory i) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues val = new ContentValues();
        val.put("name", i.getName());
        val.put("category", i.getCategory());
        val.put("total_stock", i.getTotalStock());
        val.put("available", i.getAvailable());
        val.put("damaged", i.getDamaged());
        return db.update("inventory", val, "id = ?", new String[]{String.valueOf(i.getId())}) > 0;
    }

    public List<Inventory> getInventoryList() {
        List<Inventory> list = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        try (Cursor cursor = db.rawQuery("SELECT * FROM inventory ORDER BY name ASC", null)) {
            while (cursor.moveToNext()) {
                list.add(new Inventory(
                        cursor.getInt(cursor.getColumnIndexOrThrow("id")),
                        cursor.getString(cursor.getColumnIndexOrThrow("name")),
                        cursor.getString(cursor.getColumnIndexOrThrow("category")),
                        cursor.getInt(cursor.getColumnIndexOrThrow("total_stock")),
                        cursor.getInt(cursor.getColumnIndexOrThrow("available")),
                        cursor.getInt(cursor.getColumnIndexOrThrow("damaged"))
                ));
            }
        }
        return list;
    }

    // --- CONVERSION UTILS ---

    private Student cursorToStudent(Cursor cursor) {
        Student s = new Student();
        s.setId(cursor.getInt(cursor.getColumnIndexOrThrow("id")));
        s.setPhotoUri(cursor.getString(cursor.getColumnIndexOrThrow("photo_uri")));
        s.setFullName(cursor.getString(cursor.getColumnIndexOrThrow("full_name")));
        s.setFatherName(cursor.getString(cursor.getColumnIndexOrThrow("father_name")));
        s.setMotherName(cursor.getString(cursor.getColumnIndexOrThrow("mother_name")));
        s.setRegNumber(cursor.getString(cursor.getColumnIndexOrThrow("reg_number")));
        s.setRollNumber(cursor.getString(cursor.getColumnIndexOrThrow("roll_number")));
        s.setCnic(cursor.getString(cursor.getColumnIndexOrThrow("cnic")));
        s.setPassport(cursor.getString(cursor.getColumnIndexOrThrow("passport")));
        s.setEmail(cursor.getString(cursor.getColumnIndexOrThrow("email")));
        s.setPhone(cursor.getString(cursor.getColumnIndexOrThrow("phone")));
        s.setEmergencyContact(cursor.getString(cursor.getColumnIndexOrThrow("emergency_contact")));
        s.setAddress(cursor.getString(cursor.getColumnIndexOrThrow("address")));
        s.setCity(cursor.getString(cursor.getColumnIndexOrThrow("city")));
        s.setDepartment(cursor.getString(cursor.getColumnIndexOrThrow("department")));
        s.setProgram(cursor.getString(cursor.getColumnIndexOrThrow("program")));
        s.setSemester(cursor.getString(cursor.getColumnIndexOrThrow("semester")));
        s.setGender(cursor.getString(cursor.getColumnIndexOrThrow("gender")));
        s.setBloodGroup(cursor.getString(cursor.getColumnIndexOrThrow("blood_group")));
        s.setDob(cursor.getString(cursor.getColumnIndexOrThrow("dob")));
        s.setGuardianName(cursor.getString(cursor.getColumnIndexOrThrow("guardian_name")));
        s.setGuardianContact(cursor.getString(cursor.getColumnIndexOrThrow("guardian_contact")));
        s.setPassword(cursor.getString(cursor.getColumnIndexOrThrow("password")));
        s.setSecurityQuestion(cursor.getString(cursor.getColumnIndexOrThrow("security_question")));
        s.setSecurityAnswer(cursor.getString(cursor.getColumnIndexOrThrow("security_answer")));
        s.setRegistrationDate(cursor.getString(cursor.getColumnIndexOrThrow("registration_date")));
        s.setStatus(cursor.getString(cursor.getColumnIndexOrThrow("status")));
        return s;
    }

    private Room cursorToRoom(Cursor cursor) {
        Room r = new Room();
        r.setId(cursor.getInt(cursor.getColumnIndexOrThrow("id")));
        r.setBlockId(cursor.getInt(cursor.getColumnIndexOrThrow("block_id")));
        r.setFloorId(cursor.getInt(cursor.getColumnIndexOrThrow("floor_id")));
        r.setRoomNumber(cursor.getString(cursor.getColumnIndexOrThrow("room_number")));
        r.setCapacity(cursor.getInt(cursor.getColumnIndexOrThrow("capacity")));
        r.setOccupiedSeats(cursor.getInt(cursor.getColumnIndexOrThrow("occupied_seats")));
        r.setAvailableSeats(cursor.getInt(cursor.getColumnIndexOrThrow("available_seats")));
        r.setRoomType(cursor.getString(cursor.getColumnIndexOrThrow("room_type")));
        r.setStatus(cursor.getString(cursor.getColumnIndexOrThrow("status")));
        r.setNotes(cursor.getString(cursor.getColumnIndexOrThrow("notes")));
        return r;
    }

    private Complaint cursorToComplaint(Cursor cursor) {
        Complaint c = new Complaint();
        c.setId(cursor.getInt(cursor.getColumnIndexOrThrow("id")));
        c.setStudentId(cursor.getInt(cursor.getColumnIndexOrThrow("student_id")));
        c.setCategory(cursor.getString(cursor.getColumnIndexOrThrow("category")));
        c.setTitle(cursor.getString(cursor.getColumnIndexOrThrow("title")));
        c.setDescription(cursor.getString(cursor.getColumnIndexOrThrow("description")));
        c.setStatus(cursor.getString(cursor.getColumnIndexOrThrow("status")));
        c.setRemarks(cursor.getString(cursor.getColumnIndexOrThrow("remarks")));
        c.setCreatedAt(cursor.getString(cursor.getColumnIndexOrThrow("created_at")));
        return c;
    }

    private Leave cursorToLeave(Cursor cursor) {
        Leave l = new Leave();
        l.setId(cursor.getInt(cursor.getColumnIndexOrThrow("id")));
        l.setStudentId(cursor.getInt(cursor.getColumnIndexOrThrow("student_id")));
        l.setStartDate(cursor.getString(cursor.getColumnIndexOrThrow("start_date")));
        l.setEndDate(cursor.getString(cursor.getColumnIndexOrThrow("end_date")));
        l.setDestination(cursor.getString(cursor.getColumnIndexOrThrow("destination")));
        l.setReason(cursor.getString(cursor.getColumnIndexOrThrow("reason")));
        l.setEmergencyContact(cursor.getString(cursor.getColumnIndexOrThrow("emergency_contact")));
        l.setStatus(cursor.getString(cursor.getColumnIndexOrThrow("status")));
        l.setRemarks(cursor.getString(cursor.getColumnIndexOrThrow("remarks")));
        l.setCreatedAt(cursor.getString(cursor.getColumnIndexOrThrow("created_at")));
        return l;
    }

    private Visitor cursorToVisitor(Cursor cursor) {
        Visitor v = new Visitor();
        v.setId(cursor.getInt(cursor.getColumnIndexOrThrow("id")));
        v.setStudentId(cursor.getInt(cursor.getColumnIndexOrThrow("student_id")));
        v.setName(cursor.getString(cursor.getColumnIndexOrThrow("name")));
        v.setRelation(cursor.getString(cursor.getColumnIndexOrThrow("relation")));
        v.setCnic(cursor.getString(cursor.getColumnIndexOrThrow("cnic")));
        v.setPhone(cursor.getString(cursor.getColumnIndexOrThrow("phone")));
        v.setVisitDate(cursor.getString(cursor.getColumnIndexOrThrow("visit_date")));
        v.setVisitTime(cursor.getString(cursor.getColumnIndexOrThrow("visit_time")));
        v.setPurpose(cursor.getString(cursor.getColumnIndexOrThrow("purpose")));
        v.setStatus(cursor.getString(cursor.getColumnIndexOrThrow("status")));
        return v;
    }

    private Fee cursorToFee(Cursor cursor) {
        Fee f = new Fee();
        f.setId(cursor.getInt(cursor.getColumnIndexOrThrow("id")));
        f.setStudentId(cursor.getInt(cursor.getColumnIndexOrThrow("student_id")));
        f.setCategory(cursor.getString(cursor.getColumnIndexOrThrow("category")));
        f.setAmount(cursor.getDouble(cursor.getColumnIndexOrThrow("amount")));
        f.setStatus(cursor.getString(cursor.getColumnIndexOrThrow("status")));
        f.setPaymentDate(cursor.getString(cursor.getColumnIndexOrThrow("payment_date")));
        return f;
    }
}
