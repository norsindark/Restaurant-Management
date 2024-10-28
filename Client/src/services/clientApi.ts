import axios from '../utils/axios-customize';

export const callRegister = (
  email: string,
  password: string,
  fullName: string
) => {
  const data = axios.post('/api/v1/auth/sign-up', {
    email,
    password,
    fullName,
  });
  console.log('data', data);
  return data;
};

export const callLogin = (email: string, password: string) => {
  return axios.post('/api/v1/auth/sign-in', { email, password });
};

export const callProfile = () => {
  return axios.get('/api/v1/client/user/profile');
};

export const callLogout = () => {
  return axios.get('/api/v1/client/user/logout');
};

export const callForgotPassword = (params: string) => {
  return axios.get(`/api/v1/auth/forgot-password?email=${params}`);
};

export const callResetPassword = (token: string, password: string) => {
  return axios.post('/api/v1/auth/reset-password', { token, password });
};

export const callResendVerifyEmail = (email: string) => {
  return axios.get(`/api/v1/auth/resend-verification-email?email=${email}`);
};

export const callVerifyEmail = (token: string) => {
  return axios.get(`/api/v1/auth/verify-email?token=${token}`);
};

export const callRefreshToken = () => {
  return axios.get('/api/v1/auth/refresh-token');
};

export const callUpdateProfile = (fullName: string, email: string) => {
  return axios.put('/api/v1/client/user/update', { fullName, email });
};

export const callUpdateAvatar = (file: FormData) => {
  return axios.put('/api/v1/client/user/update-avatar', file, {
    headers: {
      'Content-Type': 'multipart/form-data',
    },
  });
};

//phân trang address
export const callBulkAddress = (
  userId: string,
  pageNo: number,
  pageSize: number
) => {
  return axios.get(
    `/api/v1/client/address/get-all-address?userId=${userId}&pageNo=${pageNo}&pageSize=${pageSize}`
  );
};

export const callAddAddress = (
  street: string,
  country: string,
  city: string,
  addressType: string,
  state: string,
  commune: string,
  phoneNumber: string,
  userId: string
) => {
  return axios.post('/api/v1/client/address/add', {
    street,
    country,
    city,
    addressType,
    state,
    commune,
    phoneNumber,
    userId,
  });
};

export const callUpdateAddress = (
  id: string,
  street: string,
  country: string,
  city: string,
  addressType: string,
  commune: string,
  state: string,
  phoneNumber: string,
  userId: string
) => {
  return axios.put('/api/v1/client/address/update', {
    id,
    street,
    country,
    city,
    addressType,
    state,
    commune,
    phoneNumber,
    userId,
  });
};

export const callDeleteAddress = (addressId: string) => {
  return axios.delete(`/api/v1/client/address/delete/${addressId}`);
};

export const callChangePassword = (
  userId: string,
  oldPassword: string,

  newPassword: string
) => {
  return axios.put('/api/v1/client/user/change-password', {
    userId,
    oldPassword,
    newPassword,
  });
};

// guest 
export const callGetAllCategory = async () => {
  return axios.get(`/api/v1/auth/guest/get-all-categories?sortBy=createdAt&sortDir=asc`);
};

export const callGetAllDishes = async (query: string) => {
  return axios.get(`/api/v1/auth/guest/get-all-dishes?${query}`);
};
  
export const callGetDishDetail = async (dishId: string) => {
  return axios.get(`/api/v1/auth/guest/get-dish-by-id/${dishId}`);
};

export const callGetAllCoupon = async (query: string) => {
  return axios.get(`/api/v1/auth/guest/get-all-coupons?${query}`);
};

export const callCheckCouponUsageByCodeAndUserId = async (code: string, userId: string) => {
  return axios.get(`/api/v1/auth/guest/check-coupon-usage`, {
    params: {
      code,
      userId,
    },
  });
};



// Review
export const callCreateReview = async ( rating: number, review: string, dishId: string, userId: string) => {
  return axios.post(`/api/v1/client/review/create-review`, {  rating, review, dishId, userId });
};

export const callGetAllReviewForOneDish = async (dishId: string, query: string) => {
  return axios.get(`/api/v1/auth/guest/get-all-reviews-by-dish?dishId=${dishId}&${query}`);
};


export const callCreateReplyReview = async (reviewId: string, review: string, rating: number, dishId: string, userId: string) => {
  return axios.post(`/api/v1/client/review/reply-review`, { reviewId, review, rating, dishId, userId });
};


export const callUpdateReview = async (reviewId: string, review: string, rating: number, dishId: string, userId: string) => {
  return axios.put(`/api/v1/client/review/update-review`, { reviewId, review, rating, dishId, userId });
};

