package com.example.viewmodels;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

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
import com.example.repositories.HostelRepository;

import java.util.List;

/**
 * Enterprise MVVM ViewModel for Hostel Management System.
 * Separates UI controller layers from underlying Repository data stores.
 */
public class HostelViewModel extends AndroidViewModel {

    private final HostelRepository repository;

    private final MutableLiveData<List<Block>> blocks = new MutableLiveData<>();
    private final MutableLiveData<List<Room>> rooms = new MutableLiveData<>();
    private final MutableLiveData<List<Student>> students = new MutableLiveData<>();
    private final MutableLiveData<List<Complaint>> complaints = new MutableLiveData<>();
    private final MutableLiveData<List<Leave>> leaves = new MutableLiveData<>();
    private final MutableLiveData<List<Visitor>> visitors = new MutableLiveData<>();
    private final MutableLiveData<List<Fee>> fees = new MutableLiveData<>();
    private final MutableLiveData<List<Notice>> notices = new MutableLiveData<>();
    private final MutableLiveData<List<Staff>> staffList = new MutableLiveData<>();
    private final MutableLiveData<List<Inventory>> inventoryList = new MutableLiveData<>();

    private final MutableLiveData<Student> loggedInStudent = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isAdminLoggedIn = new MutableLiveData<>(false);
    private final MutableLiveData<String> actionStatus = new MutableLiveData<>();

    public HostelViewModel(@NonNull Application application) {
        super(application);
        this.repository = new HostelRepository(application);
        loadAllData();
    }

    public void loadAllData() {
        blocks.setValue(repository.getBlocks());
        rooms.setValue(repository.getRooms());
        students.setValue(repository.getStudents());
        complaints.setValue(repository.getComplaints());
        leaves.setValue(repository.getLeaves());
        visitors.setValue(repository.getVisitors());
        fees.setValue(repository.getFees());
        notices.setValue(repository.getNotices());
        staffList.setValue(repository.getStaffList());
        inventoryList.setValue(repository.getInventoryList());
    }

    // --- LIVE DATA ACCESSORS ---

    public LiveData<List<Block>> getBlocks() { return blocks; }
    public LiveData<List<Room>> getRooms() { return rooms; }
    public LiveData<List<Student>> getStudents() { return students; }
    public LiveData<List<Complaint>> getComplaints() { return complaints; }
    public LiveData<List<Leave>> getLeaves() { return leaves; }
    public LiveData<List<Visitor>> getVisitors() { return visitors; }
    public LiveData<List<Fee>> getFees() { return fees; }
    public LiveData<List<Notice>> getNotices() { return notices; }
    public LiveData<List<Staff>> getStaffList() { return staffList; }
    public LiveData<List<Inventory>> getInventoryList() { return inventoryList; }

    public LiveData<Student> getLoggedInStudent() { return loggedInStudent; }
    public LiveData<Boolean> getIsAdminLoggedIn() { return isAdminLoggedIn; }
    public LiveData<String> getActionStatus() { return actionStatus; }

    public void setActionStatus(String status) { actionStatus.setValue(status); }

    // --- AUTHENTICATION FLOWS ---

    public boolean adminLogin(String username, String password) {
        boolean success = repository.checkAdminLogin(username, password);
        isAdminLoggedIn.setValue(success);
        if (success) {
            loggedInStudent.setValue(null);
            loadAllData();
        }
        return success;
    }

    public boolean studentLogin(String regNum, String password) {
        Student s = repository.checkStudentLogin(regNum, password);
        loggedInStudent.setValue(s);
        if (s != null) {
            isAdminLoggedIn.setValue(false);
            loadAllData();
            return true;
        }
        return false;
    }

    public void logout() {
        loggedInStudent.setValue(null);
        isAdminLoggedIn.setValue(false);
        actionStatus.setValue("Logged out successfully");
    }

    public boolean resetStudentPassword(String regNum, String question, String answer, String newPass) {
        boolean success = repository.resetStudentPassword(regNum, question, answer, newPass);
        if (success) {
            actionStatus.setValue("Password reset successful");
        }
        return success;
    }

    // --- REGISTRATION ---

    public boolean registerStudent(Student s) {
        boolean success = repository.registerStudent(s);
        if (success) {
            students.setValue(repository.getStudents());
            actionStatus.setValue("Registration successful");
        }
        return success;
    }

    public boolean updateStudent(Student s) {
        boolean success = repository.updateStudent(s);
        if (success) {
            students.setValue(repository.getStudents());
            // Update logged-in student state if updating self
            if (loggedInStudent.getValue() != null && loggedInStudent.getValue().getId() == s.getId()) {
                loggedInStudent.setValue(s);
            }
            actionStatus.setValue("Student details updated");
        }
        return success;
    }

    public boolean deleteStudent(int id) {
        boolean success = repository.deleteStudent(id);
        if (success) {
            students.setValue(repository.getStudents());
            rooms.setValue(repository.getRooms()); // update seats occupied
            actionStatus.setValue("Student profile deleted");
        }
        return success;
    }

    // --- BLOCK CRUD ---

    public boolean addBlock(String name, String type) {
        Block b = new Block();
        b.setName(name);
        b.setType(type);
        b.setStatus("ACTIVE");
        boolean success = repository.addBlock(b);
        if (success) {
            blocks.setValue(repository.getBlocks());
            actionStatus.setValue("Block added successfully");
        }
        return success;
    }

    public boolean updateBlock(Block b) {
        boolean success = repository.updateBlock(b);
        if (success) {
            blocks.setValue(repository.getBlocks());
            actionStatus.setValue("Block updated");
        }
        return success;
    }

    public boolean deleteBlock(int id) {
        boolean success = repository.deleteBlock(id);
        if (success) {
            blocks.setValue(repository.getBlocks());
            actionStatus.setValue("Block deleted");
        }
        return success;
    }

    // --- FLOOR CRUD ---

    public boolean addFloor(int blockId, int floorNum) {
        Floor f = new Floor();
        f.setBlockId(blockId);
        f.setNumber(floorNum);
        f.setStatus("ACTIVE");
        boolean success = repository.addFloor(f);
        if (success) {
            actionStatus.setValue("Floor added successfully");
        }
        return success;
    }

    public List<Floor> getFloors(int blockId) {
        return repository.getFloorsByBlock(blockId);
    }

    // --- ROOM CRUD ---

    public boolean addRoom(int blockId, int floorId, String num, int cap, String type, String notes) {
        Room r = new Room();
        r.setBlockId(blockId);
        r.setFloorId(floorId);
        r.setRoomNumber(num);
        r.setCapacity(cap);
        r.setRoomType(type);
        r.setNotes(notes);
        boolean success = repository.addRoom(r);
        if (success) {
            rooms.setValue(repository.getRooms());
            actionStatus.setValue("Room created successfully");
        }
        return success;
    }

    public boolean updateRoom(Room r) {
        boolean success = repository.updateRoom(r);
        if (success) {
            rooms.setValue(repository.getRooms());
            actionStatus.setValue("Room updated successfully");
        }
        return success;
    }

    public boolean deleteRoom(int id) {
        boolean success = repository.deleteRoom(id);
        if (success) {
            rooms.setValue(repository.getRooms());
            actionStatus.setValue("Room deleted successfully");
        }
        return success;
    }

    // --- ALLOCATION FLOWS ---

    public boolean allocateRoom(int roomId, int studentId, String date) {
        boolean success = repository.allocateRoom(roomId, studentId, date);
        if (success) {
            rooms.setValue(repository.getRooms());
            students.setValue(repository.getStudents());
            actionStatus.setValue("Room allocated successfully");
        } else {
            actionStatus.setValue("Allocation failed. Room is fully occupied.");
        }
        return success;
    }

    public boolean deallocateRoom(int studentId) {
        boolean success = repository.deallocateRoom(studentId);
        if (success) {
            rooms.setValue(repository.getRooms());
            students.setValue(repository.getStudents());
            actionStatus.setValue("Room deallocated successfully");
        }
        return success;
    }

    public Room getRoomForStudent(int studentId) {
        return repository.getRoomForStudent(studentId);
    }

    // --- COMPLAINT FLOWS ---

    public boolean submitComplaint(String category, String title, String desc) {
        Student s = loggedInStudent.getValue();
        if (s == null) return false;
        Complaint c = new Complaint();
        c.setStudentId(s.getId());
        c.setCategory(category);
        c.setTitle(title);
        c.setDescription(desc);
        boolean success = repository.addComplaint(c);
        if (success) {
            complaints.setValue(repository.getComplaints());
            actionStatus.setValue("Complaint submitted successfully");
        }
        return success;
    }

    public List<Complaint> getMyComplaints() {
        Student s = loggedInStudent.getValue();
        if (s == null) return null;
        return repository.getComplaintsByStudent(s.getId());
    }

    public boolean updateComplaintStatus(int id, String status, String remarks) {
        boolean success = repository.updateComplaintStatus(id, status, remarks);
        if (success) {
            complaints.setValue(repository.getComplaints());
            actionStatus.setValue("Complaint status updated to: " + status);
        }
        return success;
    }

    // --- LEAVE FLOWS ---

    public boolean applyLeave(String start, String end, String dest, String reason, String contact) {
        Student s = loggedInStudent.getValue();
        if (s == null) return false;
        Leave l = new Leave();
        l.setStudentId(s.getId());
        l.setStartDate(start);
        l.setEndDate(end);
        l.setDestination(dest);
        l.setReason(reason);
        l.setEmergencyContact(contact);
        boolean success = repository.addLeave(l);
        if (success) {
            leaves.setValue(repository.getLeaves());
            actionStatus.setValue("Leave application submitted");
        }
        return success;
    }

    public List<Leave> getMyLeaves() {
        Student s = loggedInStudent.getValue();
        if (s == null) return null;
        return repository.getLeavesByStudent(s.getId());
    }

    public boolean updateLeaveStatus(int id, String status, String remarks) {
        boolean success = repository.updateLeaveStatus(id, status, remarks);
        if (success) {
            leaves.setValue(repository.getLeaves());
            actionStatus.setValue("Leave application status updated to: " + status);
        }
        return success;
    }

    // --- VISITOR FLOWS ---

    public boolean requestVisitor(String name, String relation, String cnic, String phone, String date, String time, String purpose) {
        Student s = loggedInStudent.getValue();
        if (s == null) return false;
        Visitor v = new Visitor();
        v.setStudentId(s.getId());
        v.setName(name);
        v.setRelation(relation);
        v.setCnic(cnic);
        v.setPhone(phone);
        v.setVisitDate(date);
        v.setVisitTime(time);
        v.setPurpose(purpose);
        boolean success = repository.addVisitor(v);
        if (success) {
            visitors.setValue(repository.getVisitors());
            actionStatus.setValue("Visitor approval request submitted");
        }
        return success;
    }

    public List<Visitor> getMyVisitors() {
        Student s = loggedInStudent.getValue();
        if (s == null) return null;
        return repository.getVisitorsByStudent(s.getId());
    }

    public boolean updateVisitorStatus(int id, String status) {
        boolean success = repository.updateVisitorStatus(id, status);
        if (success) {
            visitors.setValue(repository.getVisitors());
            actionStatus.setValue("Visitor status updated to: " + status);
        }
        return success;
    }

    // --- FEES FLOWS ---

    public boolean createFeeItem(int studentId, String category, double amount, String status, String pDate) {
        Fee f = new Fee();
        f.setStudentId(studentId);
        f.setCategory(category);
        f.setAmount(amount);
        f.setStatus(status);
        f.setPaymentDate(pDate);
        boolean success = repository.addFee(f);
        if (success) {
            fees.setValue(repository.getFees());
            actionStatus.setValue("Fee item generated");
        }
        return success;
    }

    public List<Fee> getMyFees() {
        Student s = loggedInStudent.getValue();
        if (s == null) return null;
        return repository.getFeesByStudent(s.getId());
    }

    public boolean payFee(int id, String date) {
        boolean success = repository.updateFeeStatus(id, "PAID", date);
        if (success) {
            fees.setValue(repository.getFees());
            actionStatus.setValue("Fee item marked as Paid");
        }
        return success;
    }

    // --- NOTICES ---

    public boolean publishNotice(String title, String desc, String priority, String pDate, String expDate) {
        Notice n = new Notice();
        n.setTitle(title);
        n.setDescription(desc);
        n.setPriority(priority);
        n.setPublishDate(pDate);
        n.setExpiryDate(expDate);
        boolean success = repository.addNotice(n);
        if (success) {
            notices.setValue(repository.getNotices());
            actionStatus.setValue("Notice published successfully");
        }
        return success;
    }

    public boolean deleteNotice(int id) {
        boolean success = repository.deleteNotice(id);
        if (success) {
            notices.setValue(repository.getNotices());
            actionStatus.setValue("Notice deleted");
        }
        return success;
    }

    // --- STAFF ---

    public boolean hireStaff(String name, String role, String phone, String email, String shift) {
        Staff s = new Staff();
        s.setName(name);
        s.setRole(role);
        s.setPhone(phone);
        s.setEmail(email);
        s.setShift(shift);
        boolean success = repository.addStaff(s);
        if (success) {
            staffList.setValue(repository.getStaffList());
            actionStatus.setValue("Staff registered successfully");
        }
        return success;
    }

    public boolean deleteStaff(int id) {
        boolean success = repository.deleteStaff(id);
        if (success) {
            staffList.setValue(repository.getStaffList());
            actionStatus.setValue("Staff removed successfully");
        }
        return success;
    }

    // --- INVENTORY ---

    public boolean addInventoryItem(String name, String category, int stock, int avail, int damaged) {
        Inventory i = new Inventory();
        i.setName(name);
        i.setCategory(category);
        i.setTotalStock(stock);
        i.setAvailable(avail);
        i.setDamaged(damaged);
        boolean success = repository.addInventory(i);
        if (success) {
            inventoryList.setValue(repository.getInventoryList());
            actionStatus.setValue("Inventory item added successfully");
        }
        return success;
    }

    public boolean updateInventoryItem(Inventory i) {
        boolean success = repository.updateInventory(i);
        if (success) {
            inventoryList.setValue(repository.getInventoryList());
            actionStatus.setValue("Inventory item updated successfully");
        }
        return success;
    }
}
