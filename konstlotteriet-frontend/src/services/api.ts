// API Client for Konstlotteriet Backend

import type {
  LoginRequest,
  LoginResponse,
  RegisterRequest,
  ChangePasswordRequest,
  Member,
  User,
  Artwork,
  Lottery,
  LotteryParticipant,
  RankingList,
} from '../types';

const API_BASE_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080/api';

class ApiClient {
  private getAuthHeader(): Record<string, string> {
    const token = localStorage.getItem('token');
    return token ? { Authorization: `Bearer ${token}` } : {};
  }

  private async request<T>(
    endpoint: string,
    options: RequestInit = {}
  ): Promise<T> {
    const url = `${API_BASE_URL}${endpoint}`;
    const headers = {
      'Content-Type': 'application/json',
      ...this.getAuthHeader(),
      ...options.headers,
    };

    const response = await fetch(url, {
      ...options,
      headers,
    });

    if (!response.ok) {
      const errorText = await response.text();
      throw new Error(errorText || `HTTP error! status: ${response.status}`);
    }

    // Handle empty responses (204 No Content)
    if (response.status === 204 || response.headers.get('content-length') === '0') {
      return {} as T;
    }

    return response.json();
  }

  // ========== Authentication ==========

  async login(credentials: LoginRequest): Promise<LoginResponse> {
    const response = await this.request<LoginResponse>('/auth/login', {
      method: 'POST',
      body: JSON.stringify(credentials),
    });

    // Store token in localStorage
    if (response.token) {
      localStorage.setItem('token', response.token);
      localStorage.setItem('user', JSON.stringify(response));
    }

    return response;
  }

  async register(data: RegisterRequest): Promise<User> {
    return this.request<User>('/auth/register', {
      method: 'POST',
      body: JSON.stringify(data),
    });
  }

  async changePassword(data: ChangePasswordRequest): Promise<void> {
    await this.request<void>('/auth/change-password', {
      method: 'POST',
      body: JSON.stringify(data),
    });
  }

  async getCurrentUser(): Promise<User> {
    return this.request<User>('/auth/me');
  }

  logout(): void {
    localStorage.removeItem('token');
    localStorage.removeItem('user');
  }

  isAuthenticated(): boolean {
    return !!localStorage.getItem('token');
  }

  getCurrentUserData(): LoginResponse | null {
    const userData = localStorage.getItem('user');
    return userData ? JSON.parse(userData) : null;
  }

  // ========== Members (Admin) ==========

  async getAllMembers(): Promise<Member[]> {
    return this.request<Member[]>('/admin/members');
  }

  async getMemberById(id: number): Promise<Member> {
    return this.request<Member>(`/admin/members/${id}`);
  }

  async createMember(member: Member): Promise<Member> {
    return this.request<Member>('/admin/members', {
      method: 'POST',
      body: JSON.stringify(member),
    });
  }

  async updateMember(id: number, member: Member): Promise<Member> {
    return this.request<Member>(`/admin/members/${id}`, {
      method: 'PUT',
      body: JSON.stringify(member),
    });
  }

  async deactivateMember(id: number): Promise<void> {
    await this.request<void>(`/admin/members/${id}`, {
      method: 'DELETE',
    });
  }

  // ========== Artworks (Admin) ==========

  async getAllArtworks(): Promise<Artwork[]> {
    return this.request<Artwork[]>('/admin/artworks');
  }

  async getArtworkById(id: number): Promise<Artwork> {
    return this.request<Artwork>(`/admin/artworks/${id}`);
  }

  async createArtwork(artwork: Artwork): Promise<Artwork> {
    return this.request<Artwork>('/admin/artworks', {
      method: 'POST',
      body: JSON.stringify(artwork),
    });
  }

  async updateArtwork(id: number, artwork: Artwork): Promise<Artwork> {
    return this.request<Artwork>(`/admin/artworks/${id}`, {
      method: 'PUT',
      body: JSON.stringify(artwork),
    });
  }

  async deleteArtwork(id: number): Promise<void> {
    await this.request<void>(`/admin/artworks/${id}`, {
      method: 'DELETE',
    });
  }

  async getArtworksByLottery(lotteryId: number): Promise<Artwork[]> {
    return this.request<Artwork[]>(`/admin/artworks/lottery/${lotteryId}`);
  }

  // ========== Lotteries (Admin) ==========

  async getAllLotteries(): Promise<Lottery[]> {
    return this.request<Lottery[]>('/admin/lotteries');
  }

  async getLotteryById(id: number): Promise<Lottery> {
    return this.request<Lottery>(`/admin/lotteries/${id}`);
  }

  async createLottery(lottery: Lottery): Promise<Lottery> {
    return this.request<Lottery>('/admin/lotteries', {
      method: 'POST',
      body: JSON.stringify(lottery),
    });
  }

  async updateLottery(id: number, lottery: Lottery): Promise<Lottery> {
    return this.request<Lottery>(`/admin/lotteries/${id}`, {
      method: 'PUT',
      body: JSON.stringify(lottery),
    });
  }

  async deleteLottery(id: number): Promise<void> {
    await this.request<void>(`/admin/lotteries/${id}`, {
      method: 'DELETE',
    });
  }

  async addParticipant(participant: LotteryParticipant): Promise<LotteryParticipant> {
    return this.request<LotteryParticipant>('/admin/lotteries/participants', {
      method: 'POST',
      body: JSON.stringify(participant),
    });
  }

  // ========== Member Portal ==========

  async getMemberProfile(): Promise<Member> {
    return this.request<Member>('/member/profile');
  }

  async getRankingListByLottery(lotteryId: number): Promise<RankingList> {
    return this.request<RankingList>(`/member/rankings/lottery/${lotteryId}`);
  }

  async createRankingListForLottery(lotteryId: number): Promise<RankingList> {
    return this.request<RankingList>(`/member/rankings/lottery/${lotteryId}`, {
      method: 'POST',
    });
  }

  async updateRankingList(
    rankingListId: number,
    items: Array<{ artworkId: number; rank: number }>
  ): Promise<RankingList> {
    return this.request<RankingList>(`/member/rankings/${rankingListId}`, {
      method: 'PUT',
      body: JSON.stringify(items),
    });
  }

  async submitRankingList(rankingListId: number): Promise<void> {
    await this.request<void>(`/member/rankings/${rankingListId}/submit`, {
      method: 'POST',
    });
  }

  async deleteRankingList(rankingListId: number): Promise<void> {
    await this.request<void>(`/member/rankings/${rankingListId}`, {
      method: 'DELETE',
    });
  }

  async getAvailableArtworksForLottery(lotteryId: number): Promise<Artwork[]> {
    return this.request<Artwork[]>(`/admin/artworks/year/${lotteryId}/available`);
  }

  // ========== Lottery Draw (Admin) ==========

  async performDraw(lotteryId: number): Promise<DrawResult> {
    return this.request<DrawResult>(`/admin/draw/lottery/${lotteryId}`, {
      method: 'POST',
    });
  }
}

export interface DrawResult {
  lotteryId: number;
  year: number;
  totalParticipants: number;
  totalWinners: number;
  totalArtworks: number;
  assignedArtworks: number;
  winners: WinnerInfo[];
}

export interface WinnerInfo {
  memberId: number;
  memberName: string;
  artworkId: number;
  artworkTitle: string;
  artworkArtist: string;
}

export const api = new ApiClient();
