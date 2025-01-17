import { createBrowserRouter } from 'react-router-dom';
import LayoutPublic from '../components/public/layout/LayoutPublic';
// import LayoutAdmin from "../pages/admin/LayoutAdmin";
import HomePage from '../pages/public/HomePage';
// import RegisterModal from '../pages/public/RegisterModal';

// import LoginModal from '../pages/public/LoginModal';
import NotFound from '../components/NotFound/NotFound';
import ProtectedRoute from '../components/ProtectedRoute/ProtectedRoute';
// import ForgotPassword from '../components/public/auth/forgotpassword/ForgotPassword';
// import ResetPassword from '../components/public/auth/resetpassword/ResetPassword';
// import ResendVerifyEmail from '../components/public/auth/resendverifyemail/ResendVerifyEmail';
// import VerifyEmail from '../components/public/auth/verifyemail/VerifyEmail';
// import Account from '../components/public/auth/account/Account';
import Main from '../components/admin/layout/Main';
import Home from '../pages/admin/Home';
import Category from '../pages/admin/categoris/Category';
import AccountAdmin from '../pages/admin/accountAdmin/AccountAdmin';
import Warehouse from '../pages/admin/warehouse/Warehouse';
import Product from '../pages/admin/product/Product';
import User from '../pages/admin/user/User';
import Order from '../pages/admin/order/Order';
import AttendanceManagement from '../pages/admin/attendanceManagement/AttendanceManagement';
import AboutPage from '../pages/public/AboutPage';
import MenuPage from '../pages/public/MenuPage';
import ProductDetail from '../pages/public/ProductDetail';
import CartPage from '../pages/public/CartPage';
import CheckoutPage from '../pages/public/CheckoutPage';
import ProductOption from '../pages/admin/productoption/ProductOption';
import Coupon from '../pages/admin/coupon/Coupon';
import Review from '../pages/admin/review/Review';
import Setting from '../pages/admin/setting/Setting';
import PaymentPage from '../pages/public/PaymentPage';
import StatusPayment from '../pages/public/StatusPayment';
import PaymentReturn from '../pages/public/PaymentReturn';
import ProductOfferDaily from '../pages/admin/productofferdaily/ProductOfferDaily';
import FaqsPage from '../pages/public/FaqsPage';
import PrivacyPolicyPage from '../pages/public/PrivacyPolicyPage';

import TermsAndConditionPage from '../pages/public/TermsAndConditionPage';
import BlogPage from '../pages/public/BlogPage';
import BlogDetail from '../pages/public/BlogDetail';
import ContactPage from '../pages/public/ContactPage';
import CategoryBlog from '../pages/admin/categorisBlog/CategoryBlog';
import Blog from '../pages/admin/blog/Blog';
import CommentsBlog from '../pages/admin/commentsBlog/CommentsBlog';
import EmployeeShift from '../pages/admin/employeeShift/EmployeeShift';
import PrivateRouter from '../components/NotFound/PrivateRouter';
import PrivatePaymentRouter from '../components/NotFound/PrivatePaymentRouter';

export const router = createBrowserRouter([
  {
    path: '/',
    element: <LayoutPublic />,
    errorElement: <NotFound />,
    children: [
      {
        path: '/',
        element: <HomePage />,
      },
      {
        path: 'register',
        element: <HomePage />,
      },
      {
        path: 'login',
        element: <HomePage />,
      },
      {
        path: 'forgot-password',
        element: <HomePage />,
      },
      {
        path: 'reset-password',
        element: <HomePage />,
      },
      {
        path: 'resend-verification-email',
        element: <HomePage />,
      },
      {
        path: 'verify-email',
        element: <HomePage />,
      },
      {
        path: `/callback`,
        element: <HomePage />,
      },
      {
        path: 'account',
        element: <HomePage />,
      },
      {
        path: '/about',
        element: <AboutPage />,
      },
      {
        path: '/faqs',
        element: <FaqsPage />,
      },
      {
        path: '/privacy-policy',
        element: <PrivacyPolicyPage />,
      },

      {
        path: '/terms-condition',
        element: <TermsAndConditionPage />,
      },
      {
        path: '/blog',
        element: <BlogPage />,
      },
      {
        path: '/blog-detail/:slug',
        element: <BlogDetail />,
      },
      {
        path: '/contact',
        element: <ContactPage />,
      },
      {
        path: '/menu',
        element: <MenuPage />,
      },
      {
        path: `/product-detail/:slug`,
        element: <ProductDetail />,
      },

      {
        path: '/cart',
        element: (
          <PrivateRouter>
            <CartPage />
          </PrivateRouter>
        ),
      },
      {
        path: '/checkout',
        element: (
          <PrivatePaymentRouter>
            <CheckoutPage />
          </PrivatePaymentRouter>
        ),
      },
      {
        path: '/payment',
        element: (
          <PrivatePaymentRouter>
            <PaymentPage />
          </PrivatePaymentRouter>
        ),
      },
      {
        path: '/status-payment',
        element: (
          <PrivatePaymentRouter>
            <StatusPayment setActiveModal={(modalName) => {}} />
          </PrivatePaymentRouter>
        ),
      },
      {
        path: '/payment/return',
        element: (
          <PrivatePaymentRouter>
            <PaymentReturn />
          </PrivatePaymentRouter>
        ),
      },
    ],
  },
  {
    path: '/',
    element: (
      <ProtectedRoute>
        <Main />
      </ProtectedRoute>
    ),
    errorElement: <NotFound />,
    children: [
      {
        index: true,
        path: 'dashboard',
        element: <Home />,
      },
      {
        path: '/user',
        element: <User />,
      },
      {
        path: '/employee-shift',
        element: <EmployeeShift />,
      },
      {
        path: '/attendance',
        element: <AttendanceManagement />,
      },
      {
        path: '/category',
        element: <Category />,
      },
      {
        path: '/account-admin',
        element: <AccountAdmin />,
      },
      {
        path: '/warehouse',
        element: <Warehouse />,
      },
      {
        path: '/product-daily-offer',
        element: <ProductOfferDaily />,
      },
      {
        path: '/product',
        element: <Product />,
      },
      {
        path: '/product-option',
        element: <ProductOption />,
      },
      {
        path: '/coupon',
        element: <Coupon />,
      },
      {
        path: '/order',
        element: <Order />,
      },
      {
        path: '/review',
        element: <Review />,
      },
      {
        path: '/setting',
        element: <Setting />,
      },
      {
        path: '/category-blog-admin',
        element: <CategoryBlog />,
      },
      {
        path: '/blog-admin',
        element: <Blog />,
      },
      {
        path: '/comments-blog-admin',
        element: <CommentsBlog />,
      },
    ],
  },
]);
