# Email Configuration Guide

This guide explains how to configure the email system for Konstlotteriet.

## Development Mode (Mock Emails)

By default, the application runs in **mock mode** where emails are not actually sent but are logged to the console. This is perfect for development and testing.

```properties
# application.properties
quarkus.mailer.mock=true
quarkus.mailer.from=noreply@konstlotteriet.se
```

### Testing Mock Emails

1. Start the application: `./mvnw quarkus:dev`
2. Use the test endpoint:
   ```bash
   curl -X POST "http://localhost:8080/api/admin/email/test?to=test@example.com" \
     -H "Authorization: Bearer YOUR_TOKEN"
   ```
3. Check the console output - you'll see the email content logged

## Production Mode (Real SMTP)

For production, you need to configure a real SMTP server. Here are the most common options:

### Option 1: Gmail

1. **Enable 2-Factor Authentication** on your Gmail account
2. **Generate an App Password**:
   - Go to Google Account → Security → 2-Step Verification → App Passwords
   - Generate a password for "Mail"
3. **Update `application.properties`**:
   ```properties
   quarkus.mailer.mock=false
   quarkus.mailer.host=smtp.gmail.com
   quarkus.mailer.port=587
   quarkus.mailer.start-tls=REQUIRED
   quarkus.mailer.username=your-email@gmail.com
   quarkus.mailer.password=your-16-char-app-password
   quarkus.mailer.from=your-email@gmail.com
   ```

**Note**: Gmail has sending limits (500 emails/day for free accounts).

### Option 2: SendGrid

SendGrid offers 100 free emails per day, perfect for small-scale use.

1. **Sign up** at https://sendgrid.com/
2. **Create an API Key**:
   - Settings → API Keys → Create API Key
   - Give it "Full Access" or "Mail Send" permissions
3. **Update `application.properties`**:
   ```properties
   quarkus.mailer.mock=false
   quarkus.mailer.host=smtp.sendgrid.net
   quarkus.mailer.port=587
   quarkus.mailer.start-tls=REQUIRED
   quarkus.mailer.username=apikey
   quarkus.mailer.password=YOUR_SENDGRID_API_KEY
   quarkus.mailer.from=noreply@konstlotteriet.se
   ```

**Note**: You need to verify the sender email address in SendGrid.

### Option 3: Mailgun

Mailgun offers 5,000 free emails per month for 3 months.

1. **Sign up** at https://www.mailgun.com/
2. **Get SMTP Credentials**:
   - Sending → Domain Settings → SMTP credentials
3. **Update `application.properties`**:
   ```properties
   quarkus.mailer.mock=false
   quarkus.mailer.host=smtp.mailgun.org
   quarkus.mailer.port=587
   quarkus.mailer.start-tls=REQUIRED
   quarkus.mailer.username=postmaster@your-domain.mailgun.org
   quarkus.mailer.password=YOUR_MAILGUN_PASSWORD
   quarkus.mailer.from=noreply@konstlotteriet.se
   ```

### Option 4: Custom SMTP Server

If you have your own SMTP server (e.g., from your hosting provider):

```properties
quarkus.mailer.mock=false
quarkus.mailer.host=smtp.yourdomain.com
quarkus.mailer.port=587  # or 465 for SSL, or 25
quarkus.mailer.start-tls=REQUIRED  # or DISABLED if using SSL
quarkus.mailer.username=your-smtp-username
quarkus.mailer.password=your-smtp-password
quarkus.mailer.from=noreply@konstlotteriet.se
```

## Email Notification Types

The system sends the following types of emails:

### 1. **Lottery Announcement**
- **Trigger**: Manual via admin endpoint
- **Recipients**: All lottery participants
- **Content**: Announces new lottery and reminds to create ranking list
- **Endpoint**: `POST /api/admin/email/lottery/{lotteryId}/announce`

### 2. **Ranking List Reminder**
- **Trigger**: Manual via admin endpoint
- **Recipients**: Participants who haven't submitted ranking lists
- **Content**: Reminds to submit ranking list before draw date
- **Endpoint**: `POST /api/admin/email/lottery/{lotteryId}/remind-rankings`

### 3. **Winner Notification**
- **Trigger**: Automatic after lottery draw OR manual
- **Recipients**: All participants (winners get different message than non-winners)
- **Content**: Congratulations for winners with artwork details, encouragement for non-winners
- **Endpoint**: Automatic in draw, or `POST /api/admin/email/lottery/{lotteryId}/notify-winners`

### 4. **Welcome Email**
- **Trigger**: Manual via admin endpoint (can be automated in Member creation)
- **Recipients**: New members
- **Content**: Welcome message and explanation of how the lottery works
- **Endpoint**: `POST /api/admin/email/member/{memberId}/welcome`

### 5. **Test Email**
- **Trigger**: Manual for testing
- **Recipients**: Specified email address
- **Content**: Simple test message
- **Endpoint**: `POST /api/admin/email/test?to=email@example.com`

## Usage Examples

### Sending Test Email

```bash
curl -X POST "http://localhost:8080/api/admin/email/test?to=admin@konstlotteriet.se" \
  -H "Authorization: Bearer YOUR_ADMIN_TOKEN"
```

### Announcing Lottery

```bash
curl -X POST "http://localhost:8080/api/admin/email/lottery/1/announce" \
  -H "Authorization: Bearer YOUR_ADMIN_TOKEN"
```

### Sending Ranking Reminders

```bash
curl -X POST "http://localhost:8080/api/admin/email/lottery/1/remind-rankings" \
  -H "Authorization: Bearer YOUR_ADMIN_TOKEN"
```

### Sending Winner Notifications

```bash
curl -X POST "http://localhost:8080/api/admin/email/lottery/1/notify-winners" \
  -H "Authorization: Bearer YOUR_ADMIN_TOKEN"
```

### Sending Welcome Email to New Member

```bash
curl -X POST "http://localhost:8080/api/admin/email/member/5/welcome" \
  -H "Authorization: Bearer YOUR_ADMIN_TOKEN"
```

## Customizing Email Templates

Email templates are defined in `EmailService.java`. To customize:

1. Open `src/main/java/se/konstlotteriet/service/EmailService.java`
2. Find the template methods (e.g., `buildWinnerEmail()`, `buildLotteryAnnouncementEmail()`)
3. Modify the text as needed
4. Save and restart the application

Example template:
```java
private String buildWinnerEmail(Member member, Artwork artwork, Lottery lottery) {
    return String.format("""
        Grattis %s!

        Du har vunnit: %s av %s

        // ... more content
        """,
        member.getFirstName(),
        artwork.getTitle(),
        artwork.getArtist()
    );
}
```

## Troubleshooting

### Emails not being sent

1. **Check mock mode**: Ensure `quarkus.mailer.mock=false` in production
2. **Verify SMTP credentials**: Double-check username/password
3. **Check logs**: Look for error messages in console
4. **Test connection**: Use the test email endpoint
5. **Firewall**: Ensure your server can connect to SMTP port (587/465/25)

### Gmail "Less secure app" error

- Gmail no longer supports "less secure apps"
- You **must** use 2FA + App Password
- Or use OAuth2 (more complex setup)

### SendGrid/Mailgun not working

- Verify your API key is correct
- Check that sender email is verified
- Ensure you're within free tier limits

### Email goes to spam

- Add SPF/DKIM/DMARC records to your domain DNS
- Use a verified sender domain
- Avoid spam trigger words in subject/body
- Test with mail-tester.com

## Best Practices

1. **Always test in mock mode first** before going live
2. **Monitor sending limits** of your provider
3. **Keep backups** of email configurations
4. **Use environment variables** for sensitive data (passwords/API keys)
5. **Log email failures** for debugging
6. **Respect user preferences** - add unsubscribe options in future
7. **Rate limit** email sending to avoid overwhelming SMTP servers

## Security Notes

⚠️ **Never commit SMTP passwords to version control!**

Use environment variables:
```properties
quarkus.mailer.password=${SMTP_PASSWORD}
```

Then set in environment:
```bash
export SMTP_PASSWORD=your-password
./mvnw quarkus:dev
```

Or use a `.env` file (add to `.gitignore`):
```
SMTP_PASSWORD=your-password
```

## Further Reading

- [Quarkus Mailer Guide](https://quarkus.io/guides/mailer)
- [SendGrid Documentation](https://docs.sendgrid.com/)
- [Mailgun Documentation](https://documentation.mailgun.com/)
- [Gmail SMTP Guide](https://support.google.com/mail/answer/7126229)
