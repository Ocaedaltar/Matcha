import { Routes } from '@angular/router';

export const routes: Routes = [
  {
    path: '',
    loadComponent: () => import('./home').then((m) => m.HomeComponent),
  },
  {
    path: 'signin',
    loadComponent: () =>
      import('../auth/sign-in/sign-in').then((m) => m.SignInComponent),
  },
  {
    path: 'signup',
    loadComponent: () =>
      import('../auth/sign-up/sign-up').then((m) => m.SignUpComponent),
  },
  {
    path: 'confirmation/:token',
    loadComponent: () =>
      import('../auth/confirmation/confirmation').then(
        (m) => m.ConfirmationComponent
      ),
  },
  { path: '**', redirectTo: '' },
];
