// API Types for Konstlotteriet

export interface LoginRequest {
  email: string;
  password: string;
}

export interface LoginResponse {
  token: string;
  email: string;
  role: string;
  userId: number;
  memberId?: number;
}

export interface RegisterRequest {
  email: string;
  password: string;
  role: string;
  memberId?: number;
}

export interface ChangePasswordRequest {
  oldPassword: string;
  newPassword: string;
}

export interface Member {
  id?: number;
  firstName: string;
  lastName: string;
  email: string;
  phoneNumber: string;
  membershipStartYear: number;
  lastWinYear?: number;
  active: boolean;
}

export interface User {
  id?: number;
  email: string;
  role: string;
  memberId?: number;
  active: boolean;
}

export interface Artwork {
  id?: number;
  title: string;
  artist: string;
  year: number;
  yearId?: number;
  lotteryId?: number;
  estimatedValue: number;
  description: string;
  imageUrl?: string;
}

export interface Lottery {
  id?: number;
  year: number;
  drawDate: string;
  status: 'PLANNED' | 'ACTIVE' | 'COMPLETED';
}

export interface LotteryParticipant {
  id?: number;
  lotteryId: number;
  memberId: number;
  numberOfTickets: number;
  hasWon: boolean;
  wonArtworkId?: number;
}

export interface RankingList {
  id?: number;
  lotteryId: number;
  memberId: number;
  items: RankingItem[];
  submittedAt?: string;
}

export interface RankingItem {
  id?: number;
  rankingListId?: number;
  artworkId: number;
  rank: number;
  artwork?: Artwork;
}

export interface ApiError {
  message: string;
  status?: number;
}
