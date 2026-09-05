import type { ButtonHTMLAttributes, PropsWithChildren } from 'react';

export function Mark() { return <span className="mark" aria-label="Fixora">✦</span>; }
export function Button({ children, className = '', ...props }: PropsWithChildren<ButtonHTMLAttributes<HTMLButtonElement>>) {
  return <button className={`button ${className}`} {...props}>{children}</button>;
}
export function Card({ children, className = '' }: PropsWithChildren<{ className?: string }>) { return <section className={`card ${className}`}>{children}</section>; }
export function Rating({ value }: { value: number }) { return <span className="rating" aria-label={`${value} out of 5 stars`}>★ {value.toFixed(1)}</span>; }
export function Empty({ title, body }: { title: string; body: string }) { return <div className="empty"><span>✦</span><h3>{title}</h3><p>{body}</p></div>; }

