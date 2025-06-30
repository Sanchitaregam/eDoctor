# eDoctor - Smart Health Management System

A comprehensive Android application built with Jetpack Compose that provides a complete healthcare management system with three user types: **Admin**, **Doctor**, and **Patient**.

## 🏥 Features Overview

### 🏢 **Admin Dashboard**
- **System Overview**: View total doctors, patients, and appointments
- **User Management**: 
  - View all registered doctors and patients
  - Delete any user (doctor/patient)
  - Approve/reject doctor registrations
- **Appointment Management**: View all appointments in the system
- **Statistics**: Real-time dashboard with key metrics

### 👨‍⚕️ **Doctor Dashboard**
- **Profile Management**: View and edit profile information
- **Appointment Management**: View upcoming appointments
- **Availability Management**: Set and manage availability schedules
- **Patient Management**: View assigned patients
- **Quick Actions**: Easy access to key features

### 👤 **Patient Dashboard**
- **Doctor Search**: Search and find available doctors
- **Appointment Booking**: Book appointments with selected doctors
- **Appointment History**: View booked appointments
- **Profile Management**: Edit personal information
- **Health Tips**: Access health-related information

## 🚀 User Flows

### Admin Flow
1. **Login** → Admin Dashboard
2. **Dashboard** → View system statistics
3. **Doctors Tab** → Manage doctor accounts
4. **Patients Tab** → Manage patient accounts
5. **Appointments Tab** → View all appointments

### Doctor Flow
1. **Login** → Doctor Dashboard
2. **Dashboard** → View upcoming appointments and stats
3. **Set Availability** → Configure working hours
4. **View Patients** → See assigned patients
5. **Edit Profile** → Update personal information

### Patient Flow
1. **Login** → Patient Dashboard
2. **Search Doctors** → Find available doctors
3. **Book Appointment** → Select doctor and schedule
4. **View Appointments** → Check booked appointments
5. **Edit Profile** → Update personal information

## 🛠 Technical Implementation

### Architecture
- **UI Framework**: Jetpack Compose
- **Database**: Room Database with SQLite
- **Navigation**: Jetpack Navigation Compose
- **State Management**: Compose State and LaunchedEffect
- **Coroutines**: For asynchronous operations

### Database Schema

#### Users Table
```kotlin
@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    var name: String = "",
    var email: String = "",
    var phone: String = "",
    var password: String = "",
    var gender: String = "",
    var role: String = "", // "doctor", "patient", or "admin"
    var dob: String? = null,
    var address: String? = null,
    var bloodGroup: String? = null,
    var emergencyContact: String? = null,
    var experience: String? = null,
    var specialization: String? = null,
    var rating: Float = 0f,
    var ratingCount: Int = 0,
    var isApproved: Boolean = true
)
```

#### Appointments Table
```kotlin
@Entity(tableName = "appointments")
data class AppointmentEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val doctorId: Int,
    val patientId: Int,
    val patientName: String,
    val date: String,
    val time: String,
    val notes: String? = null
)
```

#### Availability Table
```kotlin
@Entity(tableName = "availability")
data class AvailabilityEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val doctorId: Int,
    val days: String,
    val fromTime: String,
    val toTime: String
)
```

### Key Components

#### 1. Navigation System
- **Welcome Screen**: App entry point
- **Role Selection**: Choose user type for login/registration
- **Dashboard Screens**: Role-specific main interfaces
- **Feature Screens**: Appointment booking, profile editing, etc.

#### 2. Session Management
- **SessionManager**: Handles user authentication state
- **Auto-login**: Remembers logged-in users
- **Role-based routing**: Directs users to appropriate dashboards

#### 3. Database Operations
- **UserDao**: User CRUD operations
- **AppointmentDao**: Appointment management
- **AvailabilityDao**: Doctor availability management

## 📱 Screenshots & UI Features

### Modern Material Design 3
- **Dynamic Color Scheme**: Adapts to system theme
- **Card-based Layout**: Clean, organized information display
- **Responsive Design**: Works on various screen sizes
- **Intuitive Navigation**: Easy-to-use interface

### Key UI Components
- **Stat Cards**: Display key metrics
- **Action Cards**: Quick access to features
- **User Cards**: Display user information
- **Appointment Cards**: Show appointment details
- **Search Interface**: Find doctors easily

## 🔧 Setup & Installation

### Prerequisites
- Android Studio Arctic Fox or later
- Android SDK 21+
- Kotlin 1.5+

### Installation Steps
1. Clone the repository
2. Open project in Android Studio
3. Sync Gradle files
4. Build and run on device/emulator

### Database Setup
The app automatically creates the database on first launch. No manual setup required.

## 🎯 Key Features Implementation

### 1. Admin Dashboard
- **Tabbed Interface**: Dashboard, Doctors, Patients, Appointments
- **Real-time Statistics**: Live count of users and appointments
- **User Management**: View, delete users with confirmation dialogs
- **Comprehensive Overview**: All system data in one place

### 2. Doctor Dashboard
- **Appointment Overview**: Shows upcoming appointments
- **Quick Stats**: Today's appointments and total patients
- **Availability Management**: Easy access to set working hours
- **Profile Management**: Edit personal and professional information

### 3. Patient Dashboard
- **Doctor Search**: Search by specialty or name
- **Appointment Booking**: Streamlined booking process
- **Appointment History**: View all booked appointments
- **Health Tips**: Access health-related information

## 🔒 Security Features

### Authentication
- **Password-based Login**: Secure user authentication
- **Role-based Access**: Different features for different user types
- **Session Management**: Secure session handling

### Data Protection
- **Local Database**: All data stored locally
- **Input Validation**: Proper validation for all user inputs
- **Error Handling**: Graceful error handling throughout the app

## 📊 Performance Optimizations

### Database Operations
- **Async Operations**: All database operations run on background threads
- **Efficient Queries**: Optimized database queries
- **Lazy Loading**: Load data only when needed

### UI Performance
- **LazyColumn**: Efficient list rendering
- **State Management**: Minimal recompositions
- **Image Optimization**: Efficient image loading and caching

## 🚀 Future Enhancements

### Planned Features
- **Push Notifications**: Appointment reminders
- **Video Consultations**: In-app video calls
- **Prescription Management**: Digital prescriptions
- **Payment Integration**: Online payment processing
- **Medical Records**: Digital health records
- **Multi-language Support**: Internationalization

### Technical Improvements
- **Cloud Sync**: Remote data synchronization
- **Offline Support**: Enhanced offline capabilities
- **Analytics**: User behavior analytics
- **Testing**: Comprehensive unit and UI tests

## 🤝 Contributing

This is a college project demonstrating comprehensive Android development skills. The codebase follows best practices and is well-documented for educational purposes.

## 📄 License

This project is created for educational purposes as part of a college assignment.

---

**eDoctor** - Smart Health, Anytime, Anywhere 🏥 