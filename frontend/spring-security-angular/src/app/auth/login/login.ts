import { Component, inject } from '@angular/core';
import { Auth } from '../auth';
import { FormBuilder, FormGroup, Validators,ReactiveFormsModule } from '@angular/forms';
import { Router,RouterLink } from '@angular/router';
import { CommonModule } from '@angular/common';


@Component({
  selector: 'app-login',
  imports: [CommonModule, ReactiveFormsModule, RouterLink],
  templateUrl: './login.html',
  styleUrl: './login.css'
})
export class Login {
  form: FormGroup;
  errorMessage = '';
  isLoading = false;

  constructor(private auth: Auth, private router: Router, private formBuilder: FormBuilder) {
    this.form = this.formBuilder.group({
      username: ['', Validators.required],
      password: ['', Validators.required]
    });
  }

  
  login() {
    if (this.form.valid) {
    this.isLoading = true;
    this.errorMessage = '';
    const {username, password} = this.form.value;

    this.auth.login(username, password).subscribe({
      next: () => this.router.navigate(['/']),
      error: (error) => {
        this.errorMessage = error.error.message
        this.isLoading = false;
      },
      complete: () => {
        this.isLoading = false;
      }
    });
  }  
  }
}

