package com.fixora;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.*;
import java.util.*;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

record RegisterRequest(@NotBlank @Size(max=100) String name, @Email String email, @NotBlank @Size(min=8,max=72) String password, @NotNull Role role) {}
record LoginRequest(@Email String email, @NotBlank String password) {}
record AuthResponse(String accessToken,String refreshToken,UserView user) {}
record UserView(Long id,String name,String email,Role role) { static UserView of(User u){return new UserView(u.id,u.name,u.email,u.role);} }
record ServiceView(Long id,String name,String category,String description,BigDecimal startingPrice,String icon) { static ServiceView of(ServiceOffering s){return new ServiceView(s.id,s.name,s.category,s.description,s.startingPrice,s.icon);} }
record ProviderView(Long id,String displayName,String specialty,BigDecimal rating,int completedJobs,BigDecimal hourlyRate,String city) { static ProviderView of(ProviderProfile p){return new ProviderView(p.id,p.user.name,p.specialty,p.rating,p.completedJobs,p.hourlyRate,p.city);} }
record BookingRequest(@NotNull Long serviceId,@NotNull Long providerId,@NotBlank @Size(max=300) String address,@NotNull @Future LocalDateTime scheduledAt,@Size(max=1200) String notes) {}
record BookingView(Long id,String serviceName,String providerName,LocalDateTime scheduledAt,BookingStatus status,BigDecimal total,String address) { static BookingView of(Booking b){return new BookingView(b.id,b.service.name,b.provider.name,b.scheduledAt,b.status,b.total,b.address);} }
record StatusRequest(@NotNull BookingStatus status) {}
record MessageRequest(@NotBlank @Size(max=2000) String body) {}
record MessageView(Long id,Long roomId,String sender,String body,Instant createdAt) { static MessageView of(Message m){return new MessageView(m.id,m.room.id,m.sender.name,m.body,m.createdAt);} }
record PaymentRequest(@NotBlank @Pattern(regexp="UPI|CARD|WALLET|CASH") String method) {}
record PaymentView(Long id,Long bookingId,String method,PaymentStatus status,BigDecimal amount,String reference) { static PaymentView of(Payment p){return new PaymentView(p.id,p.booking.id,p.method,p.status,p.amount,p.reference);} }
record NotificationView(Long id,String title,String body,boolean read,Instant createdAt) { static NotificationView of(Notification n){return new NotificationView(n.id,n.title,n.body,n.read,n.createdAt);} }
record AiRequest(@NotBlank @Size(max=1000) String message) {}
record AiResponse(String reply,List<ServiceView> suggestions) {}
record AddressRequest(@NotBlank @Size(max=40) String label,@NotBlank @Size(max=120) String line1,@Size(max=120) String line2,@NotBlank @Size(max=80) String city,@NotBlank @Size(max=20) String postalCode,@Size(max=120) String landmark,boolean defaultAddress) {}
record AddressView(Long id,String label,String line1,String line2,String city,String postalCode,String landmark,boolean defaultAddress) { static AddressView of(Address a){return new AddressView(a.id,a.label,a.line1,a.line2,a.city,a.postalCode,a.landmark,a.defaultAddress);} }

@RestController @RequestMapping("/api/v1/auth")
class AuthController {
  private final UserRepository users; private final PasswordEncoder encoder; private final JwtService jwt;
  AuthController(UserRepository users,PasswordEncoder encoder,JwtService jwt){this.users=users;this.encoder=encoder;this.jwt=jwt;}
  @PostMapping("/register") @ResponseStatus(HttpStatus.CREATED) AuthResponse register(@Valid @RequestBody RegisterRequest request){
    if(users.findByEmail(request.email().trim().toLowerCase()).isPresent()) throw new ResponseStatusException(HttpStatus.CONFLICT,"An account with that email already exists.");
    var user=new User();user.name=request.name().trim();user.email=request.email().trim().toLowerCase();user.passwordHash=encoder.encode(request.password());user.role=request.role();users.save(user);return tokens(user);
  }
  @PostMapping("/login") AuthResponse login(@Valid @RequestBody LoginRequest request){var user=users.findByEmail(request.email().trim().toLowerCase()).orElseThrow(()->new ResponseStatusException(HttpStatus.UNAUTHORIZED,"Incorrect email or password."));if(!encoder.matches(request.password(),user.passwordHash))throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,"Incorrect email or password.");return tokens(user);}
  @PostMapping("/refresh") AuthResponse refresh(@RequestHeader("Authorization") String header){try{var email=jwt.parse(header.replace("Bearer ","")).getPayload().getSubject();return tokens(users.findByEmail(email).orElseThrow());}catch(Exception e){throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,"Invalid refresh token.");}}
  private AuthResponse tokens(User u){return new AuthResponse(jwt.issue(u,900),jwt.issue(u,604800),UserView.of(u));}
}

@RestController @RequestMapping("/api/v1")
class MarketplaceController {
  private final UserRepository users; private final ServiceRepository services; private final ProviderRepository providers; private final AddressRepository addresses; private final BookingRepository bookings; private final RoomRepository rooms; private final MessageRepository messages; private final NotificationRepository notifications; private final PaymentRepository payments;
  MarketplaceController(UserRepository users,ServiceRepository services,ProviderRepository providers,AddressRepository addresses,BookingRepository bookings,RoomRepository rooms,MessageRepository messages,NotificationRepository notifications,PaymentRepository payments){this.users=users;this.services=services;this.providers=providers;this.addresses=addresses;this.bookings=bookings;this.rooms=rooms;this.messages=messages;this.notifications=notifications;this.payments=payments;}
  @GetMapping("/services") List<ServiceView> serviceList(){return services.findByActiveTrueOrderByCategoryAscNameAsc().stream().map(ServiceView::of).toList();}
  @GetMapping("/providers") List<ProviderView> providerList(){return providers.findByVerifiedTrueOrderByRatingDesc().stream().map(ProviderView::of).toList();}
  @GetMapping("/users/me") UserView me(Authentication a){return UserView.of(current(a));}
  @GetMapping("/addresses") List<AddressView> addressList(Authentication a){return addresses.findByUserIdOrderByDefaultAddressDescIdDesc(current(a).id).stream().map(AddressView::of).toList();}
  @PostMapping("/addresses") @ResponseStatus(HttpStatus.CREATED) AddressView createAddress(Authentication a,@Valid @RequestBody AddressRequest request){var user=current(a);if(request.defaultAddress()) addresses.findByUserIdOrderByDefaultAddressDescIdDesc(user.id).forEach(x->{x.defaultAddress=false;addresses.save(x);});var address=new Address();address.user=user;address.label=request.label().trim();address.line1=request.line1().trim();address.line2=request.line2();address.city=request.city().trim();address.postalCode=request.postalCode().trim();address.landmark=request.landmark();address.defaultAddress=request.defaultAddress()||addresses.findByUserIdOrderByDefaultAddressDescIdDesc(user.id).isEmpty();addresses.save(address);return AddressView.of(address);}
  @PutMapping("/addresses/{id}") AddressView updateAddress(Authentication a,@PathVariable Long id,@Valid @RequestBody AddressRequest request){var address=addresses.findById(id).filter(x->x.user.id.equals(current(a).id)).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Address not found."));if(request.defaultAddress()) addresses.findByUserIdOrderByDefaultAddressDescIdDesc(address.user.id).forEach(x->{x.defaultAddress=false;addresses.save(x);});address.label=request.label().trim();address.line1=request.line1().trim();address.line2=request.line2();address.city=request.city().trim();address.postalCode=request.postalCode().trim();address.landmark=request.landmark();address.defaultAddress=request.defaultAddress();addresses.save(address);return AddressView.of(address);}
  @DeleteMapping("/addresses/{id}") @ResponseStatus(HttpStatus.NO_CONTENT) void deleteAddress(Authentication a,@PathVariable Long id){var address=addresses.findById(id).filter(x->x.user.id.equals(current(a).id)).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Address not found."));addresses.delete(address);}
  @GetMapping("/bookings/me") List<BookingView> myBookings(Authentication a){var u=current(a);return (u.role==Role.PROVIDER?bookings.findByProviderIdOrderByScheduledAtDesc(u.id):bookings.findByCustomerIdOrderByScheduledAtDesc(u.id)).stream().map(BookingView::of).toList();}
  @PostMapping("/bookings") @ResponseStatus(HttpStatus.CREATED) BookingView createBooking(Authentication a,@Valid @RequestBody BookingRequest request){
    var customer=current(a); if(customer.role!=Role.CUSTOMER)throw new ResponseStatusException(HttpStatus.FORBIDDEN,"Only customer accounts can create bookings.");
    var service=services.findById(request.serviceId()).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Service not found."));var provider=users.findById(request.providerId()).filter(u->u.role==Role.PROVIDER).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Provider not found."));
    var booking=new Booking();booking.customer=customer;booking.provider=provider;booking.service=service;booking.address=request.address().trim();booking.notes=request.notes();booking.scheduledAt=request.scheduledAt();booking.total=service.startingPrice;bookings.save(booking);
    var room=new ChatRoom();room.booking=booking;rooms.save(room);notify(provider,"New booking request",customer.name+" requested "+service.name+".");notify(customer,"Booking requested","We’ll notify you as soon as "+provider.name+" responds.");return BookingView.of(booking);
  }
  @PatchMapping("/bookings/{id}/status") BookingView updateStatus(Authentication a,@PathVariable Long id,@Valid @RequestBody StatusRequest request){var booking=bookings.findById(id).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Booking not found."));var user=current(a);if(!booking.customer.id.equals(user.id)&&!booking.provider.id.equals(user.id)&&user.role!=Role.ADMIN)throw new ResponseStatusException(HttpStatus.FORBIDDEN,"You cannot update this booking.");booking.status=request.status();bookings.save(booking);notify(booking.customer,"Booking update",booking.service.name+" is now "+request.status().name().toLowerCase().replace('_',' ')+".");return BookingView.of(booking);}
  @GetMapping("/bookings/{bookingId}/messages") List<MessageView> messageList(Authentication a,@PathVariable Long bookingId){var booking=ownedBooking(a,bookingId);var room=rooms.findByBookingId(booking.id).orElseThrow();return messages.findByRoomIdOrderByCreatedAtAsc(room.id).stream().map(MessageView::of).toList();}
  @PostMapping("/bookings/{bookingId}/messages") @ResponseStatus(HttpStatus.CREATED) MessageView sendMessage(Authentication a,@PathVariable Long bookingId,@Valid @RequestBody MessageRequest request){var booking=ownedBooking(a,bookingId);var room=rooms.findByBookingId(booking.id).orElseThrow();var message=new Message();message.room=room;message.sender=current(a);message.body=request.body().trim();messages.save(message);notify(message.sender.id.equals(booking.customer.id)?booking.provider:booking.customer,"New message",message.sender.name+" sent you a message.");return MessageView.of(message);}
  @GetMapping("/notifications") List<NotificationView> notificationList(Authentication a){return notifications.findByUserIdOrderByCreatedAtDesc(current(a).id).stream().map(NotificationView::of).toList();}
  @PatchMapping("/notifications/{id}/read") @ResponseStatus(HttpStatus.NO_CONTENT) void read(Authentication a,@PathVariable Long id){var n=notifications.findById(id).filter(x->x.user.id.equals(current(a).id)).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Notification not found."));n.read=true;notifications.save(n);}
  @PostMapping("/bookings/{bookingId}/payment") @ResponseStatus(HttpStatus.CREATED) PaymentView pay(Authentication a,@PathVariable Long bookingId,@Valid @RequestBody PaymentRequest request){var booking=ownedBooking(a,bookingId);if(!booking.customer.id.equals(current(a).id))throw new ResponseStatusException(HttpStatus.FORBIDDEN,"Only the customer can pay.");if(payments.findByBookingId(booking.id).isPresent())throw new ResponseStatusException(HttpStatus.CONFLICT,"Payment already exists.");var p=new Payment();p.booking=booking;p.method=request.method();p.amount=booking.total;p.status=PaymentStatus.PAID;p.reference="FX-"+UUID.randomUUID().toString().replace("-","").substring(0,16).toUpperCase();payments.save(p);notify(booking.customer,"Payment received","Your payment for "+booking.service.name+" was received.");return PaymentView.of(p);}
  @PostMapping("/assistant") AiResponse assistant(Authentication a,@Valid @RequestBody AiRequest request){var matches=services.findByActiveTrueOrderByCategoryAscNameAsc().stream().filter(s->(s.name+" "+s.category+" "+s.description).toLowerCase().contains(request.message().toLowerCase())).limit(3).map(ServiceView::of).toList();var reply=matches.isEmpty()?"I can help you find a trusted professional. Tell me whether this is plumbing, electrical, cleaning, appliance, or another home-care need.":"Based on that, these services look like the best fit. You can choose one and see verified local professionals.";return new AiResponse(reply,matches);}
  @GetMapping("/provider/dashboard") Map<String,Object> providerDashboard(Authentication a){var u=current(a);if(u.role!=Role.PROVIDER)throw new ResponseStatusException(HttpStatus.FORBIDDEN,"Provider account required.");var jobs=bookings.findByProviderIdOrderByScheduledAtDesc(u.id);var earnings=payments.findAll().stream().filter(p->p.booking.provider.id.equals(u.id)&&p.status==PaymentStatus.PAID).map(p->p.amount).reduce(BigDecimal.ZERO,BigDecimal::add);return Map.of("upcoming",jobs.stream().filter(b->b.scheduledAt.isAfter(LocalDateTime.now())).map(BookingView::of).toList(),"completedJobs",jobs.stream().filter(b->b.status==BookingStatus.COMPLETED).count(),"earnings",earnings);}
  @GetMapping("/admin/overview") Map<String,Object> adminOverview(Authentication a){if(current(a).role!=Role.ADMIN)throw new ResponseStatusException(HttpStatus.FORBIDDEN,"Admin account required.");return Map.of("users",users.count(),"providers",providers.count(),"bookings",bookings.count(),"payments",payments.count());}
  private User current(Authentication a){return users.findByEmail(a.getName()).orElseThrow(()->new ResponseStatusException(HttpStatus.UNAUTHORIZED,"Account not found."));}
  private Booking ownedBooking(Authentication a,Long id){var b=bookings.findById(id).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Booking not found."));var u=current(a);if(!b.customer.id.equals(u.id)&&!b.provider.id.equals(u.id)&&u.role!=Role.ADMIN)throw new ResponseStatusException(HttpStatus.FORBIDDEN,"You cannot access this booking.");return b;}
  private void notify(User user,String title,String body){var n=new Notification();n.user=user;n.title=title;n.body=body;notifications.save(n);}
}

@RestControllerAdvice
class ApiErrors {
  @ExceptionHandler(ResponseStatusException.class) ResponseEntity<Map<String,String>> status(ResponseStatusException e){return ResponseEntity.status(e.getStatusCode()).body(Map.of("message",Optional.ofNullable(e.getReason()).orElse("Request failed.")));}
  @ExceptionHandler(org.springframework.web.bind.MethodArgumentNotValidException.class) ResponseEntity<Map<String,String>> validation(org.springframework.web.bind.MethodArgumentNotValidException e){return ResponseEntity.badRequest().body(Map.of("message",e.getBindingResult().getFieldErrors().stream().findFirst().map(x->x.getField()+": "+x.getDefaultMessage()).orElse("Please check your input.")));}
}
