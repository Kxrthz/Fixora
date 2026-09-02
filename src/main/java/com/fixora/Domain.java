package com.fixora;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.*;
import java.util.*;
import org.springframework.data.jpa.repository.JpaRepository;

enum Role { CUSTOMER, PROVIDER, ADMIN }
enum BookingStatus { PENDING, CONFIRMED, IN_PROGRESS, COMPLETED, CANCELLED }
enum PaymentStatus { PENDING, PAID, REFUNDED, FAILED }

@Entity @Table(name="users")
class User {
  @Id @GeneratedValue(strategy=GenerationType.IDENTITY) Long id;
  @Column(nullable=false, length=100) String name;
  @Column(nullable=false, unique=true, length=190) String email;
  @Column(nullable=false) String passwordHash;
  @Enumerated(EnumType.STRING) @Column(nullable=false, length=20) Role role;
  @Column(nullable=false) boolean enabled=true;
  @Column(nullable=false, updatable=false) Instant createdAt=Instant.now();
}

@Entity @Table(name="services")
class ServiceOffering {
  @Id @GeneratedValue(strategy=GenerationType.IDENTITY) Long id;
  @Column(nullable=false, length=100) String name;
  @Column(nullable=false, length=60) String category;
  @Column(nullable=false, length=600) String description;
  @Column(nullable=false, precision=10, scale=2) BigDecimal startingPrice;
  @Column(nullable=false, length=12) String icon;
  @Column(nullable=false) boolean active=true;
}

@Entity @Table(name="provider_profiles")
class ProviderProfile {
  @Id Long id;
  @MapsId @OneToOne(fetch=FetchType.LAZY) @JoinColumn(name="id") User user;
  @Column(nullable=false, length=120) String specialty;
  @Column(nullable=false, precision=3, scale=2) BigDecimal rating=BigDecimal.valueOf(5);
  @Column(nullable=false) int completedJobs=0;
  @Column(nullable=false, precision=10, scale=2) BigDecimal hourlyRate;
  @Column(nullable=false, length=80) String city;
  @Column(nullable=false) boolean verified=false;
}

@Entity @Table(name="addresses")
class Address {
  @Id @GeneratedValue(strategy=GenerationType.IDENTITY) Long id;
  @ManyToOne(optional=false) @JoinColumn(name="user_id") User user;
  @Column(nullable=false, length=40) String label;
  @Column(nullable=false, length=120) String line1;
  @Column(length=120) String line2;
  @Column(nullable=false, length=80) String city;
  @Column(nullable=false, length=20) String postalCode;
  @Column(length=120) String landmark;
  @Column(nullable=false) boolean defaultAddress=false;
}

@Entity @Table(name="bookings")
class Booking {
  @Id @GeneratedValue(strategy=GenerationType.IDENTITY) Long id;
  @ManyToOne(optional=false) @JoinColumn(name="customer_id") User customer;
  @ManyToOne(optional=false) @JoinColumn(name="provider_id") User provider;
  @ManyToOne(optional=false) @JoinColumn(name="service_id") ServiceOffering service;
  @Column(nullable=false, length=300) String address;
  @Column(length=1200) String notes;
  @Column(nullable=false) LocalDateTime scheduledAt;
  @Enumerated(EnumType.STRING) @Column(nullable=false, length=20) BookingStatus status=BookingStatus.PENDING;
  @Column(nullable=false, precision=10, scale=2) BigDecimal total;
  @Column(nullable=false, updatable=false) Instant createdAt=Instant.now();
}

@Entity @Table(name="chat_rooms", uniqueConstraints=@UniqueConstraint(columnNames={"booking_id"}))
class ChatRoom { @Id @GeneratedValue(strategy=GenerationType.IDENTITY) Long id; @OneToOne(optional=false) @JoinColumn(name="booking_id") Booking booking; @Column(nullable=false) Instant createdAt=Instant.now(); }

@Entity @Table(name="messages")
class Message {
  @Id @GeneratedValue(strategy=GenerationType.IDENTITY) Long id;
  @ManyToOne(optional=false) @JoinColumn(name="room_id") ChatRoom room;
  @ManyToOne(optional=false) @JoinColumn(name="sender_id") User sender;
  @Column(nullable=false, length=2000) String body;
  @Column(nullable=false) Instant createdAt=Instant.now();
}

@Entity @Table(name="notifications")
class Notification {
  @Id @GeneratedValue(strategy=GenerationType.IDENTITY) Long id;
  @ManyToOne(optional=false) @JoinColumn(name="user_id") User user;
  @Column(nullable=false, length=120) String title;
  @Column(nullable=false, length=600) String body;
  @Column(nullable=false) boolean read=false;
  @Column(nullable=false) Instant createdAt=Instant.now();
}

@Entity @Table(name="payments")
class Payment {
  @Id @GeneratedValue(strategy=GenerationType.IDENTITY) Long id;
  @OneToOne(optional=false) @JoinColumn(name="booking_id") Booking booking;
  @Column(nullable=false, length=30) String method;
  @Enumerated(EnumType.STRING) @Column(nullable=false, length=20) PaymentStatus status=PaymentStatus.PENDING;
  @Column(nullable=false, precision=10, scale=2) BigDecimal amount;
  @Column(nullable=false, unique=true, length=64) String reference;
  @Column(nullable=false) Instant createdAt=Instant.now();
}

interface UserRepository extends JpaRepository<User,Long>{ Optional<User> findByEmail(String email); }
interface ServiceRepository extends JpaRepository<ServiceOffering,Long>{ List<ServiceOffering> findByActiveTrueOrderByCategoryAscNameAsc(); }
interface ProviderRepository extends JpaRepository<ProviderProfile,Long>{ List<ProviderProfile> findByVerifiedTrueOrderByRatingDesc(); }
interface AddressRepository extends JpaRepository<Address,Long>{ List<Address> findByUserIdOrderByDefaultAddressDescIdDesc(Long id); }
interface BookingRepository extends JpaRepository<Booking,Long>{ List<Booking> findByCustomerIdOrderByScheduledAtDesc(Long id); List<Booking> findByProviderIdOrderByScheduledAtDesc(Long id); }
interface RoomRepository extends JpaRepository<ChatRoom,Long>{ Optional<ChatRoom> findByBookingId(Long bookingId); }
interface MessageRepository extends JpaRepository<Message,Long>{ List<Message> findByRoomIdOrderByCreatedAtAsc(Long id); }
interface NotificationRepository extends JpaRepository<Notification,Long>{ List<Notification> findByUserIdOrderByCreatedAtDesc(Long id); }
interface PaymentRepository extends JpaRepository<Payment,Long>{ Optional<Payment> findByBookingId(Long id); }
