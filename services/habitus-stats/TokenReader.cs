using System.Text;

namespace Habitus.Stats;

public sealed class TokenReader
{
    private const string Prefix = "fake-token-";

    public long ReadUserId(string authorizationHeader)
    {
        if (!authorizationHeader.StartsWith("Bearer ", StringComparison.OrdinalIgnoreCase))
        {
            throw new UnauthorizedAccessException("Token ausente");
        }

        var token = authorizationHeader["Bearer ".Length..].Trim();
        if (string.IsNullOrWhiteSpace(token))
        {
            throw new UnauthorizedAccessException("Token ausente");
        }

        var decoded = DecodeBase64Url(token);
        if (!decoded.StartsWith(Prefix, StringComparison.Ordinal))
        {
            throw new UnauthorizedAccessException("Token invalido");
        }

        if (!long.TryParse(decoded[Prefix.Length..], out var userId))
        {
            throw new UnauthorizedAccessException("Token invalido");
        }

        return userId;
    }

    private static string DecodeBase64Url(string token)
    {
        var base64 = token.Replace('-', '+').Replace('_', '/');
        base64 = base64.PadRight(base64.Length + ((4 - base64.Length % 4) % 4), '=');

        try
        {
            return Encoding.UTF8.GetString(Convert.FromBase64String(base64));
        }
        catch (FormatException exception)
        {
            throw new UnauthorizedAccessException("Token invalido", exception);
        }
    }
}
