import type { ButtonHTMLAttributes } from 'react';
import styles from './Button.module.css';

type ButtonProps = ButtonHTMLAttributes<HTMLButtonElement>;

function Button({ className, ...props }: ButtonProps) {
  const classes = className ? `${styles.button} ${className}` : styles.button;
  return <button className={classes} {...props} />;
}

export default Button;
