export type Service = { id: number; name: string; category: string; description: string; startingPrice: number; icon: string };
export type Provider = { id: number; displayName: string; specialty: string; rating: number; completedJobs: number; hourlyRate: number; city: string };
export type Booking = { id: number; serviceName: string; providerName: string; scheduledAt: string; status: 'PENDING' | 'CONFIRMED' | 'IN_PROGRESS' | 'COMPLETED' | 'CANCELLED'; total: number; address: string };
export type AuthResponse = { accessToken: string; refreshToken: string; user: { id: number; name: string; email: string; role: 'CUSTOMER' | 'PROVIDER' | 'ADMIN' } };

