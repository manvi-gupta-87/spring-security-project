import { CommonModule } from '@angular/common';
import { Component } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { RouterLink, Router } from '@angular/router';
import { Auth } from '../auth';

@Component({
  selector: 'app-register',
  imports: [CommonModule,ReactiveFormsModule, RouterLink],
  templateUrl: './register.html',
  styleUrl: './register.css'
})
export class Register {
  form: FormGroup;
  errorMessage = '';
  successMessage =  '';
  isLoading = false;


  constructor(private auth:Auth, private router:Router, private formBuider:FormBuilder) {
    this.form = this.formBuider.group({
      username: ['', [Validators.required, Validators.minLength(3)]],
      password: ['', [Validators.required, Validators.minLength(6)]],  
      confirmPassword: ['', Validators.required]
    }, { validators: this.passwordMatchValidator })
  }

  passwordMatchValidator(form: FormGroup) {
    const password = form.get('password');
    const confirmPasswd = form.get('confirmPassword');

    if(password && confirmPasswd && confirmPasswd.value != password.value) {
      confirmPasswd.setErrors({ passwordMismatch: true });
      return { passwordMismatch: true };
    }
    return null;
  }

  register() {
    if(this.form.valid) {
      this.isLoading = true;
      this.errorMessage = '';
      this.successMessage = '';
      const { username, password } = this.form.value;

      this.auth.register(username, password).subscribe({
        next: () => {
           this.successMessage = 'Registration successful! Redirecting to login...';
            setTimeout(() => {
              this.router.navigate(['/login']);
            }, 2000);
        },
        error: (error) => {
          if (error.status === 409) {
            this.errorMessage = 'Username already exists. Please choose a different username.';
          } else if (error.status === 400) {
            this.errorMessage = error.error?.message || 'Invalid input. Please check your data.';
          } else {
            this.errorMessage = error.error || 'Registration failed. Please try again.';
          }
          this.isLoading = false;
        }
      })
    }
  }
}
